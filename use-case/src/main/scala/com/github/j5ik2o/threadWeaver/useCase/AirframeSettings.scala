package com.github.j5ik2o.threadWeaver.useCase

import wvlet.airframe._

object AirframeSettings {

  def design: Design =
    newDesign
      .bind[CreateThreadUseCase].to[CreateThreadUseCaseImpl]

}