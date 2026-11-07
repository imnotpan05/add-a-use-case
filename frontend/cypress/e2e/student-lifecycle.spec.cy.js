const activeStudent = {
  id: 77,
  username: 'l.park@abc.edu',
  firstName: 'Lee',
  lastName: 'Park',
  email: 'l.park@abc.edu',
  enabled: true,
  roles: 'student',
  teamId: 1,
  teamName: 'Team1'
}

describe('Student deactivation confirmation', () => {
  beforeEach(() => {
    cy.login('b.gates@abc.edu', '123456')
    cy.intercept('POST', '**/students/search', {
      body: { flag: true, code: 200, message: '', data: { content: [activeStudent], number: 0, size: 50, totalElements: 1 } }
    }).as('students')
    cy.intercept('POST', '**/students/77/deactivate', {
      body: { flag: true, code: 200, message: 'Deactivate student successfully', data: { ...activeStudent, enabled: false } }
    }).as('deactivate')
    cy.intercept('GET', '**/students/77', { body: { flag: true, code: 200, message: '', data: activeStudent } }).as('student')
    cy.visit('/students')
    cy.wait('@students')
    cy.get('[data-cy=student-edit]').click()
    cy.wait('@student')
  })

  it('leaves the account active when the warning is cancelled', () => {
    cy.get('[data-cy=student-deactivate]').click()
    cy.contains('.el-message-box__btns .el-button', 'Cancel').click()

    cy.get('[data-cy=student-deactivate]').should('be.visible')
    cy.get('@deactivate.all').should('have.length', 0)
  })

  it('sends the deactivation only after confirmation', () => {
    cy.get('[data-cy=student-deactivate]').click()
    cy.contains('.el-message-box__btns .el-button', 'Deactivate').click()

    cy.wait('@deactivate').its('request.body').should('be.empty')
    cy.contains('Student deactivated successfully').should('be.visible')
  })
})
