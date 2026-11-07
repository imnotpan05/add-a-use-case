describe('Inactive teammates in new peer evaluations', () => {
  beforeEach(() => {
    cy.login('john', '123456')
    cy.intercept('GET', '**/students/teams/*', {
      body: {
        flag: true,
        code: 200,
        message: '',
        data: [
          { id: 4, firstName: 'Active', lastName: 'Teammate', enabled: true },
          { id: 88, firstName: 'Inactive', lastName: 'Teammate', enabled: false }
        ]
      }
    })
    cy.intercept('GET', '**/sections/*', {
      body: { flag: true, code: 200, message: '', data: { rubricId: 1 } }
    })
    cy.intercept('GET', '**/rubrics/1', {
      body: {
        flag: true,
        code: 200,
        message: '',
        data: { criteria: [{ criterionId: 1, criterion: 'Quality', description: 'Quality', maxScore: 10 }] }
      }
    })
    cy.intercept('GET', '**/evaluations/evaluators/*/week/*', {
      body: { flag: true, code: 200, message: '', data: [] }
    })
  })

  it('omits inactive teammates from a new evaluation form', () => {
    cy.visit('/submit-evaluations')

    cy.contains('Active Teammate').should('be.visible')
    cy.contains('Inactive Teammate').should('not.exist')
  })
})
