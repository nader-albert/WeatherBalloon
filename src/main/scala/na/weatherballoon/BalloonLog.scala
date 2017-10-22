package na.weatherballoon

case class Location(x: Double, y: Double)
case class Temperature(value: Double, unit :String)
case class Distance(value: Double, unit :String)
case class Observatory(code: String) //AU, US, FR, or anything else

case class BalloonLog ()
