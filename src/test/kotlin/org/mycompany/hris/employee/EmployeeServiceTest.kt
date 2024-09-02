package org.mycompany.hris.employee

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mycompany.hris.employee.model.CreateEmployeeRequest
import org.mycompany.hris.employee.model.PatchEmployeeRequest
import org.mycompany.hris.exception.BadRequestException
import org.mycompany.hris.givenEmail
import org.mycompany.hris.givenEmployeeId
import org.mycompany.hris.givenName
import org.mycompany.hris.givenPosition
import org.mycompany.hris.givenSurname
import org.mycompany.hris.model.Employee
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.Position
import org.mycompany.hris.utils.inTx
import java.util.UUID

class EmployeeServiceTest {
    private val employeeRepository = mockk<EmployeeRepository>(relaxed = true)
    private val employeeService = EmployeeService(employeeRepository)

    companion object {
        private val EMPLOYEE_ID = EmployeeId(UUID.randomUUID())
    }

    @BeforeEach
    fun setUp() {
        clearMocks(employeeRepository)
        mockkStatic("org.mycompany.hris.utils.TransactionUtilsKt")
        coEvery { inTx(any<suspend () -> Unit>()) } coAnswers {
            val callback = firstArg<suspend () -> Unit>()
            callback()
        }
    }

    @AfterEach
    fun cleanUp() {
        unmockkStatic("org.mycompany.hris.utils.TransactionUtilsKt")
    }

    @Nested
    inner class CreateEmployee {
        @Test
        fun `create employee with unknown subordinates`() =
            runBlocking {
                val request = givenCreateEmployeeRequest(subordinates = setOf(givenEmployeeId()))
                coEvery { employeeRepository.isEmployeeExist(any()) } returns false

                assertThrows<BadRequestException> { employeeService.createEmployee(request) }

                coVerify(exactly = 0) {
                    employeeRepository.createEmployee(any(), request)
                }
            }

        @Test
        fun `create employee with unknown supervisor`() =
            runBlocking {
                val request = givenCreateEmployeeRequest(supervisor = givenEmployeeId())
                coEvery { employeeRepository.getEmployeeById(any()) } returns emptyList()

                assertThrows<BadRequestException> { employeeService.createEmployee(request) }

                coVerify(exactly = 0) {
                    employeeRepository.createEmployee(any(), request)
                }
            }

        @Test
        fun `create employee with supervisor in subordinates`() =
            runBlocking {
                val supervisor = givenEmployeeId()
                val request = givenCreateEmployeeRequest(supervisor = supervisor, subordinates = setOf(supervisor, givenEmployeeId()))
                coEvery { employeeRepository.getEmployeeById(any()) } returns listOf(givenEmployee(supervisor))

                assertThrows<BadRequestException> { employeeService.createEmployee(request) }

                coVerify(exactly = 0) {
                    employeeRepository.createEmployee(any(), request)
                }
            }

        @Test
        fun `create employee with higher position then supervisor`() =
            runBlocking {
                val supervisor = givenEmployeeId()
                val request =
                    givenCreateEmployeeRequest(position = Position.CTO, supervisor = supervisor, subordinates = setOf(supervisor, givenEmployeeId()))
                coEvery { employeeRepository.getEmployeeById(any()) } returns
                    listOf(
                        givenEmployee(
                            supervisor,
                            position = Position.EngineeringManager,
                        ),
                    )

                assertThrows<BadRequestException> { employeeService.createEmployee(request) }

                coVerify(exactly = 0) {
                    employeeRepository.createEmployee(any(), request)
                }
            }

        @Test
        fun `create employee`() =
            runBlocking {
                val request = givenCreateEmployeeRequest()

                employeeService.createEmployee(request)

                coVerify(exactly = 1) {
                    employeeRepository.createEmployee(any(), request)
                }
            }
    }

    @Nested
    inner class UpdateEmployee {
        @Test
        fun `update employee with empty request`() =
            runBlocking {
                val request = PatchEmployeeRequest()

                employeeService.updateEmployee(EMPLOYEE_ID, request)

                coVerify(exactly = 0) {
                    employeeRepository.getEmployeeById(any())
                    employeeRepository.updateEmployees(any(), request)
                }
            }

        @Test
        fun `update employee position with higher position then supervisor is not possible`() =
            runBlocking {
                val request =
                    PatchEmployeeRequest(
                        position = Position.CTO,
                    )
                val employee = givenEmployee(EMPLOYEE_ID, Position.SoftwareEngineer)
                val supervisor = givenEmployee(employee.supervisor!!, Position.EngineeringManager)
                coEvery { employeeRepository.getEmployeeById(EMPLOYEE_ID) } returns listOf(employee)
                coEvery { employeeRepository.getEmployeeById(employee.supervisor) } returns listOf(supervisor)

                assertThrows<BadRequestException> { employeeService.updateEmployee(EMPLOYEE_ID, request) }

                coVerify(exactly = 0) {
                    employeeRepository.updateEmployees(any(), request)
                }
            }

        @Test
        fun `not possible to update employee with unknown supervisor`() =
            runBlocking {
                val request =
                    PatchEmployeeRequest(
                        supervisor = givenEmployeeId(),
                    )
                coEvery { employeeRepository.getEmployeeById(EMPLOYEE_ID) } returns listOf(givenEmployee(EMPLOYEE_ID))
                coEvery { employeeRepository.isEmployeeExist(EMPLOYEE_ID) } returns false

                assertThrows<BadRequestException> { employeeService.updateEmployee(EMPLOYEE_ID, request) }

                coVerify(exactly = 0) {
                    employeeRepository.updateEmployees(any(), request)
                }
            }

        @Test
        fun `possible to update employee with known supervisor`() =
            runBlocking {
                val request =
                    PatchEmployeeRequest(
                        supervisor = givenEmployeeId(),
                    )
                coEvery { employeeRepository.getEmployeeById(EMPLOYEE_ID) } returns listOf(givenEmployee(EMPLOYEE_ID))
                coEvery { employeeRepository.isEmployeeExist(request.supervisor!!) } returns true

                employeeService.updateEmployee(EMPLOYEE_ID, request)

                coVerify(exactly = 1) {
                    employeeRepository.updateEmployees(any(), request)
                }
            }

        @Test
        fun `not possible to update unknown employee`() =
            runBlocking {
                val request =
                    PatchEmployeeRequest(
                        supervisor = givenEmployeeId(),
                    )
                coEvery { employeeRepository.getEmployeeById(EMPLOYEE_ID) } returns emptyList()

                assertThrows<BadRequestException> { employeeService.updateEmployee(EMPLOYEE_ID, request) }

                coVerify(exactly = 0) {
                    employeeRepository.updateEmployees(any(), request)
                }
            }

        @Test
        fun `possible to update position and supervisor with correct conditions`() =
            runBlocking {
                val request =
                    PatchEmployeeRequest(
                        supervisor = givenEmployeeId(),
                        position = Position.EngineeringManager,
                    )
                val employee = givenEmployee(EMPLOYEE_ID, Position.SoftwareEngineer)
                val supervisor = givenEmployee(employee.supervisor!!, Position.EngineeringManager)
                coEvery { employeeRepository.isEmployeeExist(any()) } returns true
                coEvery { employeeRepository.getEmployeeById(EMPLOYEE_ID) } returns listOf(employee)
                coEvery { employeeRepository.getEmployeeById(employee.supervisor) } returns listOf(supervisor)

                employeeService.updateEmployee(EMPLOYEE_ID, request)

                coVerify(exactly = 1) {
                    employeeRepository.updateEmployees(any(), request)
                }
            }

        @Test
        fun `not possible to update with unknown subordinates`() =
            runBlocking {
                val request =
                    PatchEmployeeRequest(
                        subordinates = setOf(givenEmployeeId()),
                    )
                coEvery { employeeRepository.getEmployeeById(EMPLOYEE_ID) } returns listOf(givenEmployee(EMPLOYEE_ID))
                coEvery { employeeRepository.isEmployeeExist(any()) } returns false

                assertThrows<BadRequestException> { employeeService.updateEmployee(EMPLOYEE_ID, request) }

                coVerify(exactly = 0) {
                    employeeRepository.updateEmployees(any(), request)
                }
            }

        @Test
        fun `not possible to update if supervisor is one of the subordinates`() =
            runBlocking {
                val supervisor = givenEmployeeId()
                val request =
                    PatchEmployeeRequest(
                        supervisor = supervisor,
                        subordinates = setOf(supervisor, givenEmployeeId()),
                    )
                coEvery { employeeRepository.getEmployeeById(EMPLOYEE_ID) } returns listOf(givenEmployee(EMPLOYEE_ID))
                coEvery { employeeRepository.isEmployeeExist(any()) } returns true

                assertThrows<BadRequestException> { employeeService.updateEmployee(EMPLOYEE_ID, request) }

                coVerify(exactly = 0) {
                    employeeRepository.updateEmployees(any(), request)
                }
            }
    }

    @Nested
    inner class DeleteEmployee {
        @Test
        fun `delete placeholder employee`() =
            runBlocking {
                coEvery { employeeRepository.deleteEmployeeById(EMPLOYEE_ID) } returns null

                employeeService.deleteEmployee(EMPLOYEE_ID)

                coVerify(exactly = 0) {
                    employeeRepository.getEmployeeById(any())
                    employeeRepository.updateEmployees(any(), any())
                }
            }

        @Test
        fun `delete engineering manager`() =
            runBlocking {
                val supervisor = givenEmployeeId()
                coEvery { employeeRepository.deleteEmployeeById(EMPLOYEE_ID) } returns supervisor

                employeeService.deleteEmployee(EMPLOYEE_ID)

                coVerify(exactly = 1) {
                    employeeRepository.updateSupervisor(EMPLOYEE_ID, supervisor)
                }
            }
    }

    private fun givenEmployee(
        employeeId: EmployeeId = EMPLOYEE_ID,
        position: Position = givenPosition(),
    ) = Employee(
        employeeId,
        givenName(),
        givenSurname(),
        givenEmail(),
        position,
        givenEmployeeId(),
        0,
    )

    private fun givenCreateEmployeeRequest(
        position: Position = givenPosition(),
        supervisor: EmployeeId? = null,
        subordinates: Set<EmployeeId>? = null,
    ) = CreateEmployeeRequest(
        givenName(),
        givenSurname(),
        givenEmail(),
        givenPosition(),
        supervisor,
        subordinates,
    )
}
