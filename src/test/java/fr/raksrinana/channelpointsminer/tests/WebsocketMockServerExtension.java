package fr.raksrinana.channelpointsminer.tests;

import io.netty.util.internal.ThreadLocalRandom;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.extension.*;
import java.util.Objects;

@Log4j2
public class WebsocketMockServerExtension implements Extension, BeforeAllCallback, BeforeEachCallback, AfterAllCallback, ParameterResolver{
	private final WebsocketMockServer server;
	
	public WebsocketMockServerExtension(){
		server = new WebsocketMockServer(ThreadLocalRandom.current().nextInt(10000, 30000));
	}
	
	public WebsocketMockServerExtension(int port){
		server = new WebsocketMockServer(port);
	}
	
	@Override
	public void beforeAll(ExtensionContext context){
		server.start();
	}
	
	@Override
	public void beforeEach(ExtensionContext context) throws Exception{
		server.reset();
	}
	
	@Override
	public void afterAll(ExtensionContext context) throws InterruptedException{
		server.stop(10000);
	}
	
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException{
		return Objects.equals(parameterContext.getParameter().getType(), WebsocketMockServer.class);
	}
	
	@Override
	public WebsocketMockServer resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException{
		return server;
	}
}