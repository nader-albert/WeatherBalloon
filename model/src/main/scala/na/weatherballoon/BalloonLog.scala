package na.weatherballoon

case class Location(x: Double, y: Double)
case class Temperature(value: Double, unit :String)
case class Distance(value: Double, unit :String)
case class Observatory(code: String) //AU, US, FR, or anything else

case class PrintOutput(statistics: Seq[String])

object Statistics {
    val MaxTemp                         = "MaxTemp"
    val MinTemp                         = "MinTemp"
    val MeanTemp                        = "MeanTemp"
    val TOTAL_DISTANCE                  = "TotalDist"
    val OBSERVATIONS_PER_OBSERVATORY    = "Observatory"
    val TOTAL_NUMBER_OF_OBSERVATIONS    = "Observations"
}