# Transaction Form Responsibilities Enhancement

## Overview

The transaction form is missing a critical section for managing transaction responsibilities. The API requires at least one responsible party with percentage distribution that sums to 100%. This document provides the complete implementation guide.

## Problem Statement

**Current State**: The transaction form (`add/edit_transaction_form/code.html`) does not include the responsibilities section.

**API Requirement**:
```json
{
  "responsibilities": [
    {
      "responsibleId": 1,
      "percentage": 60.0,
      "calculatedAmount": 900.00,
      "notes": "Optional notes"
    },
    {
      "responsibleId": 2,
      "percentage": 40.0,
      "calculatedAmount": 600.00
    }
  ]
}
```

**Validation Rules**:
1. At least one responsibility required
2. Total percentage must equal exactly 100%
3. Calculated amounts = (transaction amount × percentage) / 100

---

## UI/UX Design

### Visual Design

The responsibilities section should appear after the core transaction fields and before the form actions. It should include:

1. **Section Header**: Clear title with required indicator
2. **Responsibility List**: Dynamic list of responsibility items
3. **Add Button**: Prominent button to add new responsibilities
4. **Total Percentage Indicator**: Visual feedback showing total percentage
5. **Validation Messages**: Clear error messaging when invalid

### Layout Structure

```
┌─────────────────────────────────────────────┐
│ Responsabilidades *                          │
│                              [Adicionar]    │
├─────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ │
│ │ Responsável: [Dropdown ▼]              │ │
│ │ Percentual:  [60  ] %                   │ │
│ │ Valor:       [R$ 900,00] (readonly)     │ │
│ │                          [X Delete]     │ │
│ └─────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────┐ │
│ │ Responsável: [Dropdown ▼]              │ │
│ │ Percentual:  [40  ] %                   │ │
│ │ Valor:       [R$ 600,00] (readonly)     │ │
│ │                          [X Delete]     │ │
│ └─────────────────────────────────────────┘ │
├─────────────────────────────────────────────┤
│ Total: 100% ✓                               │
│ [Validation message if invalid]             │
└─────────────────────────────────────────────┘
```

---

## Implementation

### HTML Structure

```html
<!-- Responsibilities Section -->
<section class="mt-6 p-4 border border-gray-300 dark:border-gray-700 rounded-lg bg-gray-50 dark:bg-gray-800/50">
  <div class="flex items-center justify-between mb-4">
    <div>
      <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
        Responsabilidades <span class="text-red-500">*</span>
      </h3>
      <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
        Distribua o valor da transação entre as pessoas responsáveis. O total deve ser 100%.
      </p>
    </div>
    <button
      type="button"
      id="add-responsible-btn"
      class="flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-lg font-medium hover:bg-primary/90 transition-colors">
      <span class="material-symbols-outlined text-lg">add</span>
      Adicionar
    </button>
  </div>

  <!-- Responsibilities List -->
  <div id="responsibilities-list" class="space-y-3 mb-4">
    <!-- Dynamic responsibility items will be inserted here -->
  </div>

  <!-- Total Percentage Indicator -->
  <div class="mt-4 p-3 bg-white dark:bg-gray-900 rounded-lg border border-gray-200 dark:border-gray-700">
    <div class="flex items-center justify-between mb-1">
      <span class="text-sm font-medium text-gray-700 dark:text-gray-300">Total:</span>
      <span
        id="total-percentage-display"
        class="text-lg font-bold"
        :class="{
          'text-green-600 dark:text-green-400': totalPercentage === 100,
          'text-red-600 dark:text-red-400': totalPercentage !== 100
        }">
        {{ totalPercentage.toFixed(2) }}%
      </span>
    </div>
    <div id="percentage-validation-message" class="text-xs mt-1" style="display: none;">
      <span class="text-red-600 dark:text-red-400">O total deve ser exatamente 100%</span>
    </div>
    <div
      id="percentage-success-indicator"
      class="flex items-center gap-1 text-sm text-green-600 dark:text-green-400 mt-1"
      style="display: none;">
      <span class="material-symbols-outlined text-base">check_circle</span>
      <span>Percentual válido</span>
    </div>
  </div>
</section>
```

### Responsibility Item Template

```html
<div
  class="responsibility-item flex items-start gap-3 p-4 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg"
  data-responsibility-index="{{index}}">

  <!-- Responsible Dropdown -->
  <div class="flex-1">
    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
      Responsável *
    </label>
    <select
      class="responsible-select w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary/50 focus:border-primary"
      required>
      <option value="">Selecione um responsável...</option>
      <!-- Populated from API -->
    </select>
  </div>

  <!-- Percentage Input -->
  <div class="w-32">
    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
      Percentual (%) *
    </label>
    <input
      type="number"
      min="0"
      max="100"
      step="0.01"
      class="percentage-input w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary/50 focus:border-primary"
      placeholder="0.00"
      required>
  </div>

  <!-- Calculated Amount (Readonly) -->
  <div class="w-40">
    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
      Valor Calculado
    </label>
    <input
      type="text"
      readonly
      class="calculated-amount w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white"
      value="R$ 0,00">
  </div>

  <!-- Notes (Optional) -->
  <div class="flex-1">
    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
      Notas (Opcional)
    </label>
    <input
      type="text"
      class="notes-input w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary/50 focus:border-primary"
      placeholder="Observações...">
  </div>

  <!-- Delete Button -->
  <div class="flex items-end">
    <button
      type="button"
      class="remove-responsible-btn p-2 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
      :disabled="responsibilities.length === 1"
      title="Remover responsável">
      <span class="material-symbols-outlined">delete</span>
    </button>
  </div>
</div>
```

---

## JavaScript Implementation

### Responsibility Manager Class

```javascript
class TransactionResponsibilityManager {
  constructor(transactionAmount = 0) {
    this.responsibilities = [];
    this.transactionAmount = transactionAmount;
    this.availableResponsibles = [];

    this.init();
  }

  async init() {
    // Load available responsibles from API
    await this.loadResponsibles();

    // Setup event listeners
    document.getElementById('add-responsible-btn')?.addEventListener('click', () => {
      this.addResponsibility();
    });

    // Watch transaction amount changes
    document.getElementById('transaction-amount')?.addEventListener('input', (e) => {
      this.transactionAmount = parseFloat(e.target.value.replace(/[^\d,.-]/g, '').replace(',', '.')) || 0;
      this.recalculateAll();
    });
  }

  async loadResponsibles() {
    try {
      const response = await fetch('/transaction-responsibles');
      const data = await response.json();
      this.availableResponsibles = data;
    } catch (error) {
      console.error('Failed to load responsibles:', error);
      this.showError('Erro ao carregar responsáveis');
    }
  }

  addResponsibility() {
    const responsibility = {
      id: Date.now(), // Temporary ID
      responsibleId: null,
      percentage: 0,
      calculatedAmount: 0,
      notes: ''
    };

    this.responsibilities.push(responsibility);
    this.render();
    this.updateValidation();
  }

  removeResponsibility(id) {
    if (this.responsibilities.length <= 1) {
      this.showError('É necessário pelo menos um responsável');
      return;
    }

    this.responsibilities = this.responsibilities.filter(r => r.id !== id);
    this.render();
    this.updateValidation();
  }

  updateResponsibility(id, field, value) {
    const responsibility = this.responsibilities.find(r => r.id === id);
    if (!responsibility) return;

    responsibility[field] = value;

    // If percentage changed, recalculate amount
    if (field === 'percentage') {
      responsibility.calculatedAmount = this.calculateAmount(responsibility.percentage);
    }

    this.render();
    this.updateValidation();
  }

  calculateAmount(percentage) {
    return (this.transactionAmount * percentage) / 100;
  }

  recalculateAll() {
    this.responsibilities.forEach(responsibility => {
      responsibility.calculatedAmount = this.calculateAmount(responsibility.percentage);
    });
    this.render();
    this.updateValidation();
  }

  getTotalPercentage() {
    return this.responsibilities.reduce((sum, r) => sum + (parseFloat(r.percentage) || 0), 0);
  }

  isValid() {
    return this.responsibilities.length > 0 &&
           this.responsibilities.every(r => r.responsibleId) &&
           this.getTotalPercentage() === 100;
  }

  updateValidation() {
    const total = this.getTotalPercentage();
    const totalDisplay = document.getElementById('total-percentage-display');
    const errorMessage = document.getElementById('percentage-validation-message');
    const successIndicator = document.getElementById('percentage-success-indicator');

    if (total === 100) {
      totalDisplay?.classList.remove('text-red-600', 'dark:text-red-400');
      totalDisplay?.classList.add('text-green-600', 'dark:text-green-400');
      errorMessage.style.display = 'none';
      successIndicator.style.display = 'flex';
    } else {
      totalDisplay?.classList.remove('text-green-600', 'dark:text-green-400');
      totalDisplay?.classList.add('text-red-600', 'dark:text-red-400');
      errorMessage.style.display = 'block';
      successIndicator.style.display = 'none';
    }

    // Update form submit button state
    this.updateSubmitButton();
  }

  updateSubmitButton() {
    const submitButton = document.getElementById('transaction-submit');
    if (submitButton) {
      submitButton.disabled = !this.isValid();
    }
  }

  render() {
    const container = document.getElementById('responsibilities-list');
    if (!container) return;

    container.innerHTML = '';

    this.responsibilities.forEach((responsibility, index) => {
      const item = this.createResponsibilityItem(responsibility, index);
      container.appendChild(item);
    });
  }

  createResponsibilityItem(responsibility, index) {
    const item = document.createElement('div');
    item.className = 'responsibility-item flex items-start gap-3 p-4 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded-lg';
    item.dataset.responsibilityId = responsibility.id;

    item.innerHTML = `
      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          Responsável *
        </label>
        <select
          class="responsible-select w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary/50 focus:border-primary"
          data-field="responsibleId"
          required>
          <option value="">Selecione...</option>
          ${this.availableResponsibles.map(r => `
            <option value="${r.id}" ${r.id === responsibility.responsibleId ? 'selected' : ''}>
              ${r.name}
            </option>
          `).join('')}
        </select>
      </div>

      <div class="w-32">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          Percentual (%) *
        </label>
        <input
          type="number"
          min="0"
          max="100"
          step="0.01"
          class="percentage-input w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary/50 focus:border-primary"
          value="${responsibility.percentage || ''}"
          placeholder="0.00"
          required>
      </div>

      <div class="w-40">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          Valor Calculado
        </label>
        <input
          type="text"
          readonly
          class="calculated-amount w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-gray-100 dark:bg-gray-700 text-gray-900 dark:text-white"
          value="${this.formatCurrency(responsibility.calculatedAmount)}">
      </div>

      <div class="flex-1">
        <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
          Notas (Opcional)
        </label>
        <input
          type="text"
          class="notes-input w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary/50 focus:border-primary"
          value="${responsibility.notes || ''}"
          placeholder="Observações...">
      </div>

      <div class="flex items-end">
        <button
          type="button"
          class="remove-responsible-btn p-2 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors ${this.responsibilities.length === 1 ? 'opacity-50 cursor-not-allowed' : ''}"
          ${this.responsibilities.length === 1 ? 'disabled' : ''}
          title="Remover responsável">
          <span class="material-symbols-outlined">delete</span>
        </button>
      </div>
    `;

    // Attach event listeners
    const responsibleSelect = item.querySelector('.responsible-select');
    const percentageInput = item.querySelector('.percentage-input');
    const notesInput = item.querySelector('.notes-input');
    const removeBtn = item.querySelector('.remove-responsible-btn');

    responsibleSelect?.addEventListener('change', (e) => {
      this.updateResponsibility(responsibility.id, 'responsibleId', parseInt(e.target.value));
    });

    percentageInput?.addEventListener('input', (e) => {
      const value = parseFloat(e.target.value) || 0;
      // Clamp to 0-100
      const clampedValue = Math.max(0, Math.min(100, value));
      if (clampedValue !== value) {
        e.target.value = clampedValue;
      }
      this.updateResponsibility(responsibility.id, 'percentage', clampedValue);
    });

    notesInput?.addEventListener('input', (e) => {
      this.updateResponsibility(responsibility.id, 'notes', e.target.value);
    });

    removeBtn?.addEventListener('click', () => {
      this.removeResponsibility(responsibility.id);
    });

    return item;
  }

  formatCurrency(value) {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value || 0);
  }

  getFormData() {
    return this.responsibilities.map(r => ({
      responsibleId: r.responsibleId,
      percentage: parseFloat(r.percentage),
      calculatedAmount: parseFloat(r.calculatedAmount.toFixed(2)),
      notes: r.notes || null
    }));
  }

  showError(message) {
    // Use toast notification system
    Toast.error(message);
  }
}

// Initialize when page loads
let responsibilityManager;

document.addEventListener('DOMContentLoaded', () => {
  const amountInput = document.getElementById('transaction-amount');
  const initialAmount = parseFloat(amountInput?.value.replace(/[^\d,.-]/g, '').replace(',', '.')) || 0;

  responsibilityManager = new TransactionResponsibilityManager(initialAmount);

  // Add initial responsibility if none exist
  if (responsibilityManager.responsibilities.length === 0) {
    responsibilityManager.addResponsibility();
  }
});
```

### Form Submission Integration

```javascript
// Update form submission handler
document.getElementById('transaction-form')?.addEventListener('submit', async (e) => {
  e.preventDefault();

  // Validate responsibilities
  if (!responsibilityManager.isValid()) {
    Toast.error('Por favor, ajuste os percentuais para totalizar exatamente 100%');
    return;
  }

  const formData = {
    type: document.getElementById('transaction-type').value,
    subtype: document.getElementById('transaction-subtype').value,
    source: document.getElementById('transaction-source').value,
    description: document.getElementById('transaction-description').value,
    amount: parseFloat(document.getElementById('transaction-amount').value.replace(/[^\d,.-]/g, '').replace(',', '.')),
    date: document.getElementById('transaction-date').value,
    categoryId: parseInt(document.getElementById('transaction-category').value),
    subcategoryId: document.getElementById('transaction-subcategory').value || null,
    sourceEntityId: document.getElementById('transaction-source-entity').value || null,
    installments: document.getElementById('transaction-installments').value || null,
    responsibilities: responsibilityManager.getFormData()
  };

  try {
    const response = await fetch('/transactions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify(formData)
    });

    if (response.ok) {
      Toast.success('Transação criada com sucesso!');
      // Close modal or redirect
    } else {
      const error = await response.json();
      Toast.error(error.message || 'Erro ao criar transação');
    }
  } catch (error) {
    Toast.error('Erro ao criar transação. Tente novamente.');
  }
});
```

---

## Edge Cases Handling

### 1. Single Responsibility (100%)
- Automatically set percentage to 100% when only one responsible
- Disable delete button when only one remains
- Show clear message: "Esta transação é de responsabilidade única"

### 2. Percentage Exceeds 100%
- Prevent input of values that would exceed 100%
- Show warning when total approaches 100%
- Auto-adjust if multiple inputs would exceed 100%

### 3. Decimal Precision
- Allow decimal percentages (e.g., 33.33%)
- Handle rounding issues carefully
- Accept 99.99% + 0.01% = 100%

### 4. Transaction Amount Changes
- Recalculate all amounts when transaction amount changes
- Maintain percentages
- Update calculated amounts in real-time

### 5. Editing Existing Transaction
- Load existing responsibilities from API
- Populate form with existing data
- Allow modifications

---

## Validation Rules

### Client-Side Validation

```javascript
function validateResponsibilities() {
  const errors = [];

  // At least one required
  if (responsibilityManager.responsibilities.length === 0) {
    errors.push('Adicione pelo menos um responsável');
  }

  // All must have responsible selected
  responsibilityManager.responsibilities.forEach((r, index) => {
    if (!r.responsibleId) {
      errors.push(`Responsável ${index + 1} deve ser selecionado`);
    }

    if (!r.percentage || r.percentage <= 0) {
      errors.push(`Responsável ${index + 1} deve ter um percentual maior que 0%`);
    }
  });

  // Total must be 100%
  const total = responsibilityManager.getTotalPercentage();
  if (Math.abs(total - 100) > 0.01) {
    errors.push(`O total dos percentuais deve ser exatamente 100%. Atual: ${total.toFixed(2)}%`);
  }

  return errors;
}
```

---

## User Flow

### Adding a Transaction with Responsibilities

1. User fills transaction details (type, amount, description, etc.)
2. User clicks "Adicionar" in responsibilities section
3. System adds empty responsibility item
4. User selects responsible from dropdown
5. User enters percentage (e.g., 60%)
6. System automatically calculates amount (e.g., R$ 600,00 for R$ 1.000,00 transaction)
7. User adds second responsibility
8. User enters remaining percentage (e.g., 40%)
9. System shows total: 100% ✓
10. User can add optional notes
11. Form validation passes
12. User submits form
13. System sends data to API with responsibilities array

---

## Testing Checklist

### Functional Tests
- [ ] Add responsibility button works
- [ ] Remove responsibility button works
- [ ] Cannot remove last responsibility
- [ ] Percentage input accepts decimals
- [ ] Percentage input clamps to 0-100
- [ ] Calculated amount updates automatically
- [ ] Total percentage indicator shows correctly
- [ ] Validation prevents submission when total ≠ 100%
- [ ] Form submission includes responsibilities data
- [ ] Editing existing transaction loads responsibilities

### Edge Cases
- [ ] Single responsibility (100%) works
- [ ] Multiple responsibilities sum to exactly 100%
- [ ] Decimal percentages (33.33% + 33.33% + 33.34%)
- [ ] Transaction amount change recalculates all
- [ ] Large amounts calculate correctly
- [ ] Very small percentages (< 1%) work

### UX Tests
- [ ] Clear error messages
- [ ] Visual feedback for valid/invalid state
- [ ] Responsive layout on mobile
- [ ] Keyboard navigation works
- [ ] Screen reader announces status

---

## API Integration Points

### Required Endpoints

1. **GET /transaction-responsibles**
   - Load available responsibles for dropdown
   - Response: `[{id: number, name: string}]`

2. **POST /transactions**
   - Include responsibilities in request body
   - Expected format matches `TransactionDTO`

3. **GET /transactions/{id}** (for editing)
   - Load transaction with responsibilities
   - Response includes responsibilities array

4. **PUT /transactions/{id}**
   - Update transaction with modified responsibilities

---

## Accessibility Considerations

1. **ARIA Labels**:
   ```html
   <div role="group" aria-labelledby="responsibilities-heading">
     <h3 id="responsibilities-heading">Responsabilidades</h3>
   </div>
   ```

2. **Live Region for Validation**:
   ```html
   <div aria-live="polite" aria-atomic="true" id="validation-status"></div>
   ```

3. **Keyboard Navigation**:
   - Tab order: Responsible → Percentage → Notes → Delete
   - Enter on percentage input updates calculated amount
   - Delete key removes responsibility (when not last)

4. **Screen Reader Announcements**:
   - Announce when percentage total reaches 100%
   - Announce validation errors
   - Announce calculated amounts

---

## Success Criteria

✅ **Implementation Complete When**:
- [ ] Users can add multiple responsibilities
- [ ] Percentage validation works correctly
- [ ] Calculated amounts update automatically
- [ ] Total percentage indicator is accurate
- [ ] Form cannot submit when invalid
- [ ] Data is correctly sent to API
- [ ] Editing existing transactions works
- [ ] All edge cases handled
- [ ] Accessible and keyboard navigable
- [ ] Works on mobile devices

---

**Document Version**: 1.0
**Last Updated**: 2025-01-XX
**Related Documents**:
- `docs/UI_UX_ANALYSIS.md`
- `docs/UI_UX_INTERACTIONS_GUIDE.md`



