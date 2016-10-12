/**
 * Created by adam on 9/28/16.
 */

package myhello;


import com.codahale.metrics.*;
import java.util.concurrent.TimeUnit;
import java.util.Random;
import com.wavefront.integrations.metrics.WavefrontReporter;


public class SayHello {

    static final MetricRegistry metrics = new MetricRegistry();


    public static void main(String args[]) {

        //Start a reporter
        startWavefrontReporter();

        //Console reporter will not be used in the blog, but is a good debugging tool
        //startConsoleReport();

        //declare a counter named runcount
        Counter runcount = metrics.counter("runcount");

        //declare a timer named timer
        final Timer timer = metrics.timer(MetricRegistry.name(SayHello.class, "loopTime"));


        //declare a histogram named
        final Histogram randomHist = metrics.histogram(MetricRegistry.name(SayHello.class, "randomHist"));


        //declare and register a gauge
        metrics.register(MetricRegistry.name(SayHello.class,
             "random"), new Gauge<Integer>() {
               public Integer getValue() {
                    return randomNum(100);
                }
            });


        for(int i=0;i<5000;i++) {
            final Timer.Context context = timer.time();

            //Increment runCount
            runcount.inc(randomNum(25));

            //Take a measurement for the histogram
            randomHist.update(randomNum(100));

            //Introduce a wait so the timer will have interesting data
            waitTime(randomNum(10));


            //Stop Timer
            context.stop();

        }


    }


    //Start a Wavefront Reporter
    static void startWavefrontReporter() {
        WavefrontReporter wfreporter = WavefrontReporter.forRegistry(metrics).
                withSource("homemade")
                .withPointTag("dc", "local")
                .withPointTag("service", "myMetrics")
                //Commenting withJvmMetrics() out. Interesting information but too dense for a walkthrough
                //.withJvmMetrics()
                .build("localhost", 2878);
        wfreporter.start(3, TimeUnit.SECONDS);
    }

    //Start the Console Reporter
    static void startConsoleReport() {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(3, TimeUnit.SECONDS);
    }

    //Wait 'time' seconds
    static void waitTime(int time) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            System.out.println("failed in wait. big error");
        }
    }

    //Generate a random number
    static int randomNum(int ceiling) {

        Random rand = new Random();
        return rand.nextInt(ceiling);
    }


}