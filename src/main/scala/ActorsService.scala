import zio.console._
import zio._
import zio.actors.Actor.Stateful
import zio.actors._
import zio.actors.Supervisor
import zio.clock.Clock
import zio.macros.accessible

sealed trait Command[+_]

case object IncreaseCounter extends Command[Int]
case object GetState extends Command[Int]

case class Room(name: String, counter: Int)

@accessible
object ActorsService {
  type ActorsService = Has[Service]

  trait Service {
    def createRoom(id: String): RIO[Clock, Unit]
    def getRooms: RIO[Console, List[Room]]
  }

  lazy val stateful: Stateful[Any, Int, Command] = new Stateful[Any, Int, Command] {
    override def receive[A](state: Int, msg: Command[A], context: Context): UIO[(Int, A)] =
      msg match {
        case IncreaseCounter => UIO((state + 1, state + 1))
        case GetState => UIO((state, state))
      }
  }

  class ServiceImpl(system: ActorSystem, registryRef: Ref[List[ActorRef[Command]]]) extends Service {
    override def createRoom(id: String): RIO[Clock, Unit] = for {
      actor <- system.make("room-" + id, Supervisor.none, 1, stateful)
      _ <- registryRef.update(l => actor :: l)
    } yield ()

    override def getRooms: RIO[Console, List[Room]] = for {
      registry <- registryRef.get
      states <- ZIO.foreach(registry)(a => for {
        p <- a.path
        s <- a ? GetState
      } yield (Room(p, s)))
    } yield(states)
  }

  lazy val live: ZLayer[Any, Throwable, Has[Service]] = (for {
    system <- ActorSystem("mySystem")
    registryRef <- Ref.make[List[ActorRef[Command]]](List())
  } yield new ServiceImpl(system, registryRef)).toLayer
}


