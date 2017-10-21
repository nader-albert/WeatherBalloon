package na.weatherballoon

/**
 * @author nader albert
 * @since  11/11/2015.
 */
//<timestamp>|<location>|<temperature>|<observatory>
case class Location(x: Int, y: Int)
case class Temperature(value: Int, unit: String)
case class Observatory(code: String) //AU, US, FR, or anything else

case class BalloonLog ()
