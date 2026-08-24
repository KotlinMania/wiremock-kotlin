#if canImport(Testing)
import Testing
import Wiremock

@Suite("Wiremock Swift Export Smoke Tests")
struct WiremockExportTests {
    @Test("Swift module loads and basic response template works")
    func swiftModuleLoads() {
        let template = ResponseTemplate(statusCode: 200, headers: [:], body: nil, delay: nil)
        #expect(template.statusCode == 200)
    }
}
#elseif canImport(XCTest)
import XCTest
import Wiremock

final class WiremockExportTests: XCTestCase {
    func testSwiftModuleLoads() throws {
        let template = ResponseTemplate(statusCode: 200, headers: [:], body: nil, delay: nil)
        XCTAssertEqual(template.statusCode, 200)
    }
}
#endif
