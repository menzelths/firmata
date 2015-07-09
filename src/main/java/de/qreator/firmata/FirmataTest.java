
package de.qreator.firmata;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.firmata4j.IODevice;
import org.firmata4j.IODeviceEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;

public class FirmataTest {
    static int port=0,modus=0; // 0 mac, 1 rpi, 2 windows
    static final String [] betriebsmodus={"Macbook","Raspberry PI","Windows"};
    static final int [] ports={8080,80,8080};
    static final String [] geraet={"/dev/tty.usbmodem1411","/dev/ttyACM0","COM3"}; // hier die adresse des arduino eingeben, eventuell anders als hier eingetragen
    static String usb="", IP="";
    static boolean weiter = true;

    public static void main(String[] s) {
        
        try{
            IP=InetAddress.getLocalHost().getHostAddress();
        } catch(Exception e){}
        System.out.println("Modus 0: Macbook, Modus 1: Raspberry PI, Modus 2: Windows");
        
        if (s.length==1){
            modus=Integer.parseInt(s[0]); // betriebsmodus festlegen: 
            System.out.println("Betriebsmodus: "+betriebsmodus[modus]);
            
        }
        port=ports[modus];
        usb=geraet[modus];
        if (s.length==2) {
            usb=s[0];
            port=Integer.parseInt(s[1]);
        }
        
        int[] schwellenwert = {60, 150, 250};
        Pin[] pinLED = new Pin[4];

        IODevice device = new FirmataDevice(usb); 
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        
        Router router = Router.router(vertx); 
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        PermittedOptions inboundPermitted = new PermittedOptions().setAddress("de.qreator.lampe");
        PermittedOptions outboundPermitted = new PermittedOptions().setAddress("de.qreator.led");
        BridgeOptions options = new BridgeOptions().addInboundPermitted(inboundPermitted).addOutboundPermitted(outboundPermitted);

        sockJSHandler.bridge(options);
        
        router.route("/eventbus/*").handler(sockJSHandler);
        router.route("/*").handler(StaticHandler.create()); // webroot unter src/main/resources/webroot        
        server.requestHandler(router::accept).listen(port);

        EventBus eb = vertx.eventBus();
        
        MessageConsumer<JsonObject> consumer = eb.consumer("de.qreator.lampe");
        consumer.handler(message -> {
            String typ = (message.body()).getString("typ");
            if (typ.equals("einaus")) {
                String statustext = message.body().getString("status");
                int status = Integer.parseInt(statustext);
                try {
                    pinLED[3].setValue(status);
                    device.sendMessage("LCD0_LED auf "+status);
                } catch (Exception e) {
                }
            }
        });
        
        try {
            device.start(); // verbindung mit arduino starten
            device.ensureInitializationIsDone(); // warten, bis verbindung komplett hergestellt wurde

            device.sendMessage("LCD0_"+IP);
            device.sendMessage("LCD1_");
            
            for (int i = 0; i < 4; i++) {
                pinLED[i] = device.getPin(2 + i);
                pinLED[i].setMode(Pin.Mode.OUTPUT);
            }

            device.addEventListener(new IODeviceEventListener() {
                @Override
                public void onStart(IOEvent event) {
                    System.out.println("Gerät ist bereit");
                }

                @Override
                public void onStop(IOEvent event) {
                    System.out.println("Gerät wurde angehalten");
                }

                @Override
                public void onPinChange(IOEvent event) {
                    // bei veränderungen an pins reagieren
                    Pin pin = event.getPin();
                    if (pin.getIndex() == 14) {
                        int zaehler = 0;

                        for (int i = 0; i < 3; i++) {
                            try {
                                if (pin.getValue() > schwellenwert[i]) {
                                    pinLED[i].setValue(1);
                                    zaehler++;
                                } else {
                                    pinLED[i].setValue(0);
                                }
                            } catch (Exception e) {
                            }
                        }
                        try {
                            device.sendMessage("LCD1_Helligkeit: "+pin.getValue()+"  ");
                        } catch (IOException ex) {
                        }
                        eb.publish("de.qreator.led", "" + zaehler); // wert an eventbus schicken
                        if (zaehler == 0) { // wenn alle lampen aus sind
                            weiter = false;
                        }
                    }
                }

                @Override
                public void onMessageReceive(IOEvent event, String message) {
                    System.out.println(message);
                }
            });

            while (weiter == true) {
                Thread.sleep(500);
            }
            device.sendMessage("LCD0_Programm beendet!");
            device.sendMessage("LCD1_");
        } catch (Exception ex) {
            Logger.getLogger(FirmataTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (device != null) {
                try {
                    for (int i = 0; i < 4; i++) { // alle lampen ausschalten
                        try {
                            pinLED[i].setValue(0);
                        } catch (Exception e) {
                        }
                    }
                    device.stop();
                    vertx.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Beendet, Gerät heruntergefahren");
            }
        }
    }
}
