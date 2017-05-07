import org.scalatestplus.play._
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime

class ApplicationSpec extends PlaySpec {
  "DateTimeFormat" must {
    "return 1970 as the beginning of epoch" in {
      val beginning = new DateTime(0)
      val formattedYear = DateTimeFormat.forPattern("YYYY").print(beginning)
      formattedYear mustBe "1970"
    }
  }
}
