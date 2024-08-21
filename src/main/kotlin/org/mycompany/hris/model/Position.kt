package org.mycompany.hris.model

// For simplicity, I prepared a small list of possible positions
enum class Position(
    val order: Int,
) {
    CEO(1),
    CTO(2),
    CPO(2),
    EngineeringManager(3),
    ProductManager(3),
    SoftwareEngineer(999),
}
