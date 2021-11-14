package test.reactor.netty.quic;

import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.incubator.codec.quic.InsecureQuicTokenHandler;
import io.netty.incubator.codec.quic.QuicSslContext;
import io.netty.incubator.codec.quic.QuicSslContextBuilder;
import reactor.netty.Connection;
import reactor.netty.incubator.quic.QuicServer;

import java.time.Duration;

public class Server {
    public static void main(String[] args) throws Exception {

        SelfSignedCertificate ssc = new SelfSignedCertificate();
        QuicSslContext serverCtx =
                QuicSslContextBuilder.forServer(ssc.privateKey(), null, ssc.certificate())
                        .applicationProtocols("http/1.1")
                        .build();

        Connection server =
                QuicServer.create()
                        .host("127.0.0.1")
                        .port(9999)
                        .secure(serverCtx)
                        .tokenHandler(InsecureQuicTokenHandler.INSTANCE)
                        .wiretap(true)
                        .idleTimeout(Duration.ofSeconds(5))
                        .initialSettings(spec ->
                                spec.maxData(10000000)
                                        .maxStreamDataBidirectionalRemote(1000000)
                                        .maxStreamsBidirectional(100))
                        .handleStream((in, out) -> out.send(in.receive().retain()))
                        .bindNow();

        System.out.println("Server listening");

        server.onDispose()
                .block();

    }
}
