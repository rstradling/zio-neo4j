import zio.test._

object DummySpec extends ZIOSpecDefault {
  def spec = suite("example test that succeeds") {
    test("Simple comparison works") {
      val obtained = 42
      val expected = 42
      assertTrue(obtained == expected)
    }
  }
}
