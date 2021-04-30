package hr.tjakopan.yarl.test.helpers;

public class TestResultClass {
  private final TestResult resultCode;
  private final String someString;

  public TestResultClass(final TestResult resultCode, final String someString) {
    this.resultCode = resultCode;
    this.someString = someString;
  }

  public TestResultClass(final TestResult resultCode) {
    this.resultCode = resultCode;
    this.someString = null;
  }

  public TestResult getResultCode() {
    return resultCode;
  }

  public String getSomeString() {
    return someString;
  }
}
