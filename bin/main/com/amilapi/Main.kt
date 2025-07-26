package com.amilapi

import io.javalin.Javalin
import io.javalin.http.staticfiles.Location

public fun main() {
  val app = Javalin.create { 
    config -> config.staticFiles.add("/frontend", Location.CLASSPATH) 
  }
  app.start(8080)
}
