import zhttp.http._
import zhttp.service.{EventLoopGroup, Server}
import zhttp.service.server.ServerChannelFactory
import zio._


object Main extends App {

  private val app = HttpApp.collectM {
    case Method.GET -> Root / "api" / "get_rooms" => ActorsService.getRooms.map(r => Response.text(r.toString()))
    case Method.GET -> Root / "api" / "create_room" => ActorsService.createRoom("test").as(Response.text("ok"))
  }

  private val server =
    Server.port(8088) ++ // Setup port
      Server.app(app) // Setup the Http app

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    server.make
      .use(_ =>
        // Waiting for the server to start
        console.putStrLn(s"Server started on port 8088")

          // Ensures the server doesn't die after printing
          *> ZIO.never,
      )
      .provideCustomLayer(ServerChannelFactory.auto ++ ActorsService.live ++ EventLoopGroup.auto(4))
      .exitCode
  }

}