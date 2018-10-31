package com.github.igorramazanov.chat.components
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{
  BackendScope,
  Callback,
  CallbackTo,
  ReactEventFromInput,
  ScalaComponent
}
import scalacss.DevDefaults._
import scalacss.ScalaCssReact._

@SuppressWarnings(Array("org.wartremover.warts.Any"))
object WelcomeComponent {
  object Styles extends StyleSheet.Inline {
    import dsl._
    val welcome = style(
      height(100.vh),
      flexDirection.column
    )
  }
  final case class Props(signIn: (String, String) => Callback,
                         signUp: (String, String) => Callback,
                         isInFlight: Boolean)

  final case class State(username: String,
                         password: String,
                         isFirstTime: Boolean) {
    def isEmpty: Boolean = username.isEmpty || password.isEmpty
  }

  object State {
    def init: State = State("", "", isFirstTime = true)
  }

  final class Backend($ : BackendScope[Props, State]) {
    private def validate: CallbackTo[Unit] =
      $.modState { s =>
        if (s.isEmpty) {
          s.copy(isFirstTime = false)
        } else {
          s
        }
      }

    private def signIn: CallbackTo[Unit] =
      for {
        p <- $.props
        s <- $.state
        _ <- validate
        _ <- if (!s.isEmpty) p.signIn(s.username, s.password)
        else Callback.empty
      } yield ()

    private def signUp: CallbackTo[Unit] =
      for {
        p <- $.props
        s <- $.state
        _ <- validate
        _ <- if (!s.isEmpty) p.signUp(s.username, s.password)
        else Callback.empty
      } yield ()

    private def inputClass(isFirstTime: Boolean, str: String) =
      if (!isFirstTime && str.isEmpty) "form-control is-invalid"
      else "form-control"

    private def button(isInFlight: Boolean, text: String, c: Callback) = {
      if (isInFlight) {
        <.button(^.disabled := true,
                 ^.className := "btn btn-primary",
                 ^.onClick --> c,
                 text)
      } else {
        <.button(^.className := "btn btn-primary", ^.onClick --> c, text)
      }
    }

    def render(p: Props, s: State): VdomElement = {
      <.div(
        ^.className := "container",
        <.div(
          ^.className := "row align-items-center",
          <.div(
            ^.className := "col-10 col-md-6 offset-md-3 d-flex justify-content-center",
            Styles.welcome,
            <.div(
              <.input(
                ^.`type` := "text",
                ^.className := inputClass(s.isFirstTime, s.username),
                ^.placeholder := "Username",
                ^.value := s.username,
                ^.onChange ==> { e: ReactEventFromInput =>
                  e.persist()
                  $.modState(_.copy(username = e.target.value))
                }
              )
            ),
            <.div(
              ^.className := "my-3",
              <.input(
                ^.`type` := "password",
                ^.className := inputClass(s.isFirstTime, s.password),
                ^.placeholder := "Password",
                ^.value := s.password,
                ^.onChange ==> { e: ReactEventFromInput =>
                  e.persist()
                  $.modState(_.copy(password = e.target.value))
                }
              )
            ),
            <.div(
              ^.className := "d-flex justify-content-between",
              button(p.isInFlight, "Sign In", signIn),
              button(p.isInFlight, "Sign Up", signUp)
            )
          )
        )
      )
    }
  }

  val Component = ScalaComponent
    .builder[Props]("Welcome")
    .initialState(State.init)
    .renderBackend[Backend]
    //.configure(Reusability.shouldComponentUpdate)
    .build
}
