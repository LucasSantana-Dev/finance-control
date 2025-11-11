# UI/UX Interactions Enhancement Guide

This document provides detailed implementation guides for all missing interactions identified in the UI/UX prototypes.

---

## 1. Filter Sidebar Toggle

### Overview
The Transactions screen requires a collapsible filter sidebar that can be toggled open/closed. On mobile, it should appear as a drawer overlay.

### Implementation

#### HTML Structure

```html
<!-- Filter Toggle Button (in toolbar) -->
<button
  id="filter-toggle"
  class="flex items-center gap-2 text-sm font-medium px-4 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700"
  aria-expanded="false"
  aria-controls="filter-sidebar">
  <span class="material-symbols-outlined text-xl">filter_list</span>
  <span>Filtros</span>
  <span id="active-filter-count" class="hidden ml-1 bg-primary text-white text-xs rounded-full px-2 py-0.5">0</span>
</button>

<!-- Filter Sidebar -->
<aside
  id="filter-sidebar"
  class="fixed inset-y-0 left-0 z-50 w-80 bg-white dark:bg-gray-900 shadow-xl transform -translate-x-full transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:shadow-none"
  aria-label="Filtros de transações">

  <!-- Backdrop for mobile -->
  <div
    id="filter-backdrop"
    class="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
    aria-hidden="true"
    style="display: none;"></div>

  <div class="flex flex-col h-full">
    <!-- Header -->
    <div class="flex items-center justify-between p-4 border-b border-gray-200 dark:border-gray-700">
      <h2 class="text-lg font-bold">Filtros</h2>
      <button
        id="filter-close"
        class="lg:hidden p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg">
        <span class="material-symbols-outlined">close</span>
      </button>
    </div>

    <!-- Filter Content -->
    <div class="flex-1 overflow-y-auto p-4 space-y-4">
      <!-- Search -->
      <div>
        <label class="block text-sm font-medium mb-2">Pesquisar</label>
        <input
          type="text"
          id="filter-search"
          class="w-full px-3 py-2 border rounded-lg ..."
          placeholder="Descrição...">
      </div>

      <!-- Date Range -->
      <div>
        <label class="block text-sm font-medium mb-2">Período</label>
        <div class="grid grid-cols-2 gap-2">
          <input
            type="date"
            id="filter-start-date"
            class="w-full px-3 py-2 border rounded-lg ...">
          <input
            type="date"
            id="filter-end-date"
            class="w-full px-3 py-2 border rounded-lg ...">
        </div>
      </div>

      <!-- Transaction Type -->
      <div>
        <label class="block text-sm font-medium mb-2">Tipo</label>
        <select id="filter-type" class="w-full px-3 py-2 border rounded-lg ...">
          <option value="">Todos</option>
          <option value="INCOME">Receita</option>
          <option value="EXPENSE">Despesa</option>
        </select>
      </div>

      <!-- Category -->
      <div>
        <label class="block text-sm font-medium mb-2">Categoria</label>
        <select id="filter-category" class="w-full px-3 py-2 border rounded-lg ...">
          <option value="">Todas</option>
          <!-- Populate from API -->
        </select>
      </div>

      <!-- Subcategory (dependent on category) -->
      <div id="filter-subcategory-wrapper" class="hidden">
        <label class="block text-sm font-medium mb-2">Subcategoria</label>
        <select id="filter-subcategory" class="w-full px-3 py-2 border rounded-lg ...">
          <option value="">Todas</option>
          <!-- Populate dynamically -->
        </select>
      </div>

      <!-- Source (multi-select) -->
      <div>
        <label class="block text-sm font-medium mb-2">Fonte</label>
        <div class="space-y-2">
          <label class="flex items-center">
            <input type="checkbox" value="CREDIT_CARD" class="filter-source">
            <span class="ml-2">Cartão de Crédito</span>
          </label>
          <label class="flex items-center">
            <input type="checkbox" value="BANK_TRANSACTION" class="filter-source">
            <span class="ml-2">Transação Bancária</span>
          </label>
          <label class="flex items-center">
            <input type="checkbox" value="DEBIT_CARD" class="filter-source">
            <span class="ml-2">Cartão de Débito</span>
          </label>
          <label class="flex items-center">
            <input type="checkbox" value="CASH" class="filter-source">
            <span class="ml-2">Dinheiro</span>
          </label>
          <label class="flex items-center">
            <input type="checkbox" value="PIX" class="filter-source">
            <span class="ml-2">PIX</span>
          </label>
          <label class="flex items-center">
            <input type="checkbox" value="BANK_TRANSFER" class="filter-source">
            <span class="ml-2">Transferência</span>
          </label>
        </div>
      </div>

      <!-- Amount Range -->
      <div>
        <label class="block text-sm font-medium mb-2">Valor</label>
        <div class="grid grid-cols-2 gap-2">
          <input
            type="number"
            id="filter-min-amount"
            placeholder="Mínimo"
            step="0.01"
            class="w-full px-3 py-2 border rounded-lg ...">
          <input
            type="number"
            id="filter-max-amount"
            placeholder="Máximo"
            step="0.01"
            class="w-full px-3 py-2 border rounded-lg ...">
        </div>
      </div>

      <!-- Installments Toggle -->
      <div>
        <label class="flex items-center justify-between">
          <span class="text-sm font-medium">Apenas parceladas</span>
          <input type="checkbox" id="filter-installments" class="toggle ...">
        </label>
      </div>
    </div>

    <!-- Footer -->
    <div class="p-4 border-t border-gray-200 dark:border-gray-700 space-y-2">
      <button
        id="filter-apply"
        class="w-full bg-primary text-white py-2 rounded-lg font-medium hover:bg-primary/90">
        Aplicar Filtros
      </button>
      <button
        id="filter-reset"
        class="w-full bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 py-2 rounded-lg font-medium hover:bg-gray-300 dark:hover:bg-gray-600">
        Limpar Filtros
      </button>
    </div>
  </div>
</aside>
```

#### JavaScript Implementation

```javascript
class FilterSidebar {
  constructor() {
    this.isOpen = false;
    this.activeFiltersCount = 0;
    this.init();
  }

  init() {
    const toggleBtn = document.getElementById('filter-toggle');
    const sidebar = document.getElementById('filter-sidebar');
    const backdrop = document.getElementById('filter-backdrop');
    const closeBtn = document.getElementById('filter-close');

    // Toggle sidebar
    toggleBtn?.addEventListener('click', () => this.toggle());

    // Close on backdrop click (mobile)
    backdrop?.addEventListener('click', () => this.close());

    // Close button
    closeBtn?.addEventListener('click', () => this.close());

    // Close on ESC key
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && this.isOpen) {
        this.close();
      }
    });

    // Load saved state
    this.loadState();

    // Watch for filter changes
    this.watchFilters();
  }

  toggle() {
    if (this.isOpen) {
      this.close();
    } else {
      this.open();
    }
  }

  open() {
    const sidebar = document.getElementById('filter-sidebar');
    const backdrop = document.getElementById('filter-backdrop');
    const toggleBtn = document.getElementById('filter-toggle');

    sidebar?.classList.remove('-translate-x-full');
    backdrop?.style.setProperty('display', 'block');
    toggleBtn?.setAttribute('aria-expanded', 'true');

    this.isOpen = true;

    // Focus trap
    this.trapFocus(sidebar);

    // Save state (desktop only)
    if (window.innerWidth >= 1024) {
      localStorage.setItem('filterSidebarOpen', 'true');
    }
  }

  close() {
    const sidebar = document.getElementById('filter-sidebar');
    const backdrop = document.getElementById('filter-backdrop');
    const toggleBtn = document.getElementById('filter-toggle');

    sidebar?.classList.add('-translate-x-full');
    backdrop?.style.setProperty('display', 'none');
    toggleBtn?.setAttribute('aria-expanded', 'false');

    this.isOpen = false;

    // Release focus
    this.releaseFocus();

    // Save state (desktop only)
    if (window.innerWidth >= 1024) {
      localStorage.setItem('filterSidebarOpen', 'false');
    }
  }

  loadState() {
    // Only restore on desktop
    if (window.innerWidth >= 1024) {
      const saved = localStorage.getItem('filterSidebarOpen');
      if (saved === 'true') {
        this.open();
      }
    }
  }

  watchFilters() {
    // Count active filters
    const inputs = document.querySelectorAll('#filter-sidebar input, #filter-sidebar select');
    inputs.forEach(input => {
      input.addEventListener('change', () => this.updateFilterCount());
    });

    // Category change updates subcategory
    document.getElementById('filter-category')?.addEventListener('change', (e) => {
      this.loadSubcategories(e.target.value);
    });
  }

  updateFilterCount() {
    let count = 0;

    // Count non-empty inputs
    const inputs = document.querySelectorAll('#filter-sidebar input[type="text"], #filter-sidebar input[type="number"], #filter-sidebar input[type="date"]');
    inputs.forEach(input => {
      if (input.value) count++;
    });

    // Count selected checkboxes
    const checked = document.querySelectorAll('#filter-sidebar input[type="checkbox"]:checked');
    count += checked.length;

    // Count non-default selects
    const selects = document.querySelectorAll('#filter-sidebar select');
    selects.forEach(select => {
      if (select.value) count++;
    });

    this.activeFiltersCount = count;

    const countBadge = document.getElementById('active-filter-count');
    if (count > 0) {
      countBadge?.classList.remove('hidden');
      countBadge.textContent = count.toString();
    } else {
      countBadge?.classList.add('hidden');
    }
  }

  trapFocus(element) {
    const focusableElements = element.querySelectorAll(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    const firstElement = focusableElements[0];
    const lastElement = focusableElements[focusableElements.length - 1];

    element.addEventListener('keydown', (e) => {
      if (e.key === 'Tab') {
        if (e.shiftKey && document.activeElement === firstElement) {
          e.preventDefault();
          lastElement.focus();
        } else if (!e.shiftKey && document.activeElement === lastElement) {
          e.preventDefault();
          firstElement.focus();
        }
      }
    });

    firstElement?.focus();
  }

  releaseFocus() {
    // Return focus to toggle button
    document.getElementById('filter-toggle')?.focus();
  }

  loadSubcategories(categoryId) {
    if (!categoryId) {
      document.getElementById('filter-subcategory-wrapper')?.classList.add('hidden');
      return;
    }

    // API call to get subcategories
    fetch(`/transaction-subcategories/category/${categoryId}`)
      .then(res => res.json())
      .then(subcategories => {
        const select = document.getElementById('filter-subcategory');
        select.innerHTML = '<option value="">Todas</option>';

        subcategories.forEach(sub => {
          const option = document.createElement('option');
          option.value = sub.id;
          option.textContent = sub.name;
          select.appendChild(option);
        });

        document.getElementById('filter-subcategory-wrapper')?.classList.remove('hidden');
      });
  }

  getFilters() {
    return {
      search: document.getElementById('filter-search')?.value || null,
      startDate: document.getElementById('filter-start-date')?.value || null,
      endDate: document.getElementById('filter-end-date')?.value || null,
      type: document.getElementById('filter-type')?.value || null,
      category: document.getElementById('filter-category')?.value || null,
      subcategory: document.getElementById('filter-subcategory')?.value || null,
      source: Array.from(document.querySelectorAll('.filter-source:checked')).map(cb => cb.value),
      minAmount: document.getElementById('filter-min-amount')?.value || null,
      maxAmount: document.getElementById('filter-max-amount')?.value || null,
      installments: document.getElementById('filter-installments')?.checked || null
    };
  }

  reset() {
    document.querySelectorAll('#filter-sidebar input, #filter-sidebar select').forEach(input => {
      if (input.type === 'checkbox') {
        input.checked = false;
      } else {
        input.value = '';
      }
    });
    this.updateFilterCount();
  }
}

// Initialize
const filterSidebar = new FilterSidebar();
```

### CSS Animations

```css
/* Sidebar slide animation */
#filter-sidebar {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Backdrop fade animation */
#filter-backdrop {
  transition: opacity 0.3s ease-in-out;
}

/* Mobile adjustments */
@media (max-width: 1023px) {
  #filter-sidebar {
    width: 320px;
    max-width: 85vw;
  }
}
```

### Testing Checklist

- [ ] Sidebar opens/closes on toggle button click
- [ ] Sidebar closes on backdrop click (mobile)
- [ ] Sidebar closes on ESC key
- [ ] Sidebar closes on close button
- [ ] Focus trap works correctly
- [ ] State persists on desktop (localStorage)
- [ ] Filter count badge updates correctly
- [ ] Subcategories load when category selected
- [ ] Responsive behavior: drawer on mobile, sidebar on desktop
- [ ] All filters apply correctly to API call

---

## 2. Modal States for Forms

### Overview
Forms for Add/Edit Transaction and Add/Edit Goal should be displayed in modal overlays rather than full-page layouts.

### Implementation

#### Modal Component Structure

```html
<!-- Modal Component -->
<div
  id="transaction-modal"
  class="fixed inset-0 z-50 overflow-y-auto"
  aria-labelledby="modal-title"
  aria-modal="true"
  role="dialog"
  style="display: none;">

  <!-- Backdrop -->
  <div
    class="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
    id="modal-backdrop"></div>

  <!-- Modal Container -->
  <div class="flex min-h-full items-center justify-center p-4">
    <div
      class="relative bg-white dark:bg-gray-900 rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden transform transition-all"
      id="modal-content">

      <!-- Header -->
      <div class="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
        <h2 id="modal-title" class="text-2xl font-bold">Nova Transação</h2>
        <button
          id="modal-close"
          class="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg"
          aria-label="Fechar modal">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>

      <!-- Loading Overlay -->
      <div
        id="modal-loading"
        class="absolute inset-0 bg-white dark:bg-gray-900 bg-opacity-75 flex items-center justify-center z-10"
        style="display: none;">
        <div class="flex flex-col items-center gap-2">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
          <p class="text-sm text-gray-600 dark:text-gray-400">Salvando...</p>
        </div>
      </div>

      <!-- Form Content -->
      <div class="p-6 overflow-y-auto max-h-[calc(90vh-140px)]">
        <!-- Form fields here -->
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-end gap-3 p-6 border-t border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800">
        <button
          id="modal-cancel"
          class="px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700 rounded-lg">
          Cancelar
        </button>
        <button
          id="modal-save"
          class="px-6 py-2 bg-primary text-white rounded-lg font-medium hover:bg-primary/90">
          Salvar
        </button>
      </div>
    </div>
  </div>
</div>
```

#### JavaScript Modal Class

```javascript
class Modal {
  constructor(modalId) {
    this.modal = document.getElementById(modalId);
    this.backdrop = this.modal?.querySelector('#modal-backdrop');
    this.content = this.modal?.querySelector('#modal-content');
    this.closeBtn = this.modal?.querySelector('#modal-close');
    this.cancelBtn = this.modal?.querySelector('#modal-cancel');
    this.loadingOverlay = this.modal?.querySelector('#modal-loading');

    this.hasUnsavedChanges = false;
    this.onCloseCallback = null;

    this.init();
  }

  init() {
    // Close on backdrop click
    this.backdrop?.addEventListener('click', (e) => {
      if (e.target === this.backdrop) {
        this.close();
      }
    });

    // Close on close button
    this.closeBtn?.addEventListener('click', () => this.close());

    // Close on cancel button
    this.cancelBtn?.addEventListener('click', () => this.close());

    // Close on ESC key
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && this.isOpen()) {
        this.close();
      }
    });

    // Watch for form changes
    this.watchFormChanges();
  }

  open() {
    this.modal?.style.setProperty('display', 'block');
    document.body.style.overflow = 'hidden';

    // Focus trap
    this.trapFocus();

    // Animate in
    setTimeout(() => {
      this.backdrop?.classList.add('opacity-100');
      this.content?.classList.add('scale-100', 'opacity-100');
    }, 10);
  }

  close(force = false) {
    if (!force && this.hasUnsavedChanges) {
      if (!confirm('Você tem alterações não salvas. Deseja realmente fechar?')) {
        return;
      }
    }

    // Animate out
    this.backdrop?.classList.remove('opacity-100');
    this.content?.classList.remove('scale-100', 'opacity-100');

    setTimeout(() => {
      this.modal?.style.setProperty('display', 'none');
      document.body.style.overflow = '';
      this.releaseFocus();

      if (this.onCloseCallback) {
        this.onCloseCallback();
      }
    }, 300);
  }

  showLoading() {
    this.loadingOverlay?.style.setProperty('display', 'flex');
  }

  hideLoading() {
    this.loadingOverlay?.style.setProperty('display', 'none');
  }

  trapFocus() {
    const focusableElements = this.modal?.querySelectorAll(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    const firstElement = focusableElements?.[0];
    const lastElement = focusableElements?.[focusableElements.length - 1];

    const handleTab = (e) => {
      if (e.key !== 'Tab') return;

      if (e.shiftKey && document.activeElement === firstElement) {
        e.preventDefault();
        lastElement?.focus();
      } else if (!e.shiftKey && document.activeElement === lastElement) {
        e.preventDefault();
        firstElement?.focus();
      }
    };

    this.modal?.addEventListener('keydown', handleTab);
    firstElement?.focus();
  }

  releaseFocus() {
    // Return focus to trigger element
    const trigger = document.activeElement;
    // Store trigger before opening, restore after closing
  }

  watchFormChanges() {
    const form = this.modal?.querySelector('form');
    if (!form) return;

    const inputs = form.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
      input.addEventListener('change', () => {
        this.hasUnsavedChanges = true;
      });
    });
  }

  isOpen() {
    return this.modal?.style.display === 'block';
  }
}

// Usage
const transactionModal = new Modal('transaction-modal');

// Open modal
document.getElementById('add-transaction-btn')?.addEventListener('click', () => {
  transactionModal.open();
});

// Submit form
document.getElementById('transaction-form')?.addEventListener('submit', async (e) => {
  e.preventDefault();

  transactionModal.showLoading();

  try {
    const formData = new FormData(e.target);
    const response = await fetch('/transactions', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(Object.fromEntries(formData))
    });

    if (response.ok) {
      transactionModal.hideLoading();
      showToast('Transação criada com sucesso!', 'success');
      transactionModal.close(true);
      // Refresh transaction list
    } else {
      throw new Error('Failed to save');
    }
  } catch (error) {
    transactionModal.hideLoading();
    showToast('Erro ao salvar transação', 'error');
  }
});
```

#### CSS Styles

```css
/* Modal animations */
#modal-content {
  transform: scale(0.95) translateY(20px);
  opacity: 0;
  transition: transform 0.3s ease-out, opacity 0.3s ease-out;
}

#modal-content.scale-100 {
  transform: scale(1) translateY(0);
  opacity: 1;
}

#modal-backdrop {
  opacity: 0;
  transition: opacity 0.3s ease-out;
}

#modal-backdrop.opacity-100 {
  opacity: 1;
}
```

---

## 3. Loading States

### Overview
All async operations need visual feedback through loading states.

### Implementation Patterns

#### Skeleton Loaders

```html
<!-- Table Row Skeleton -->
<tr class="animate-pulse">
  <td class="h-[72px] px-4 py-2">
    <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-24"></div>
  </td>
  <td class="h-[72px] px-4 py-2">
    <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-48"></div>
  </td>
  <td class="h-[72px] px-4 py-2">
    <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-32"></div>
  </td>
  <td class="h-[72px] px-4 py-2">
    <div class="h-6 bg-gray-200 dark:bg-gray-700 rounded-full w-20"></div>
  </td>
  <td class="h-[72px] px-4 py-2">
    <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-24"></div>
  </td>
  <td class="h-[72px] px-4 py-2">
    <div class="h-8 w-8 bg-gray-200 dark:bg-gray-700 rounded-full"></div>
  </td>
  <td class="h-[72px] px-4 py-2">
    <div class="h-6 bg-gray-200 dark:bg-gray-700 rounded w-16"></div>
  </td>
</tr>

<!-- Card Skeleton -->
<div class="animate-pulse rounded-lg bg-card-light dark:bg-card-dark p-6 border">
  <div class="h-6 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-4"></div>
  <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/2 mb-2"></div>
  <div class="h-4 bg-gray-200 dark:bg-gray-700 rounded w-2/3"></div>
</div>

<!-- Chart Skeleton -->
<div class="animate-pulse h-64 bg-gray-200 dark:bg-gray-700 rounded-lg"></div>
```

#### Button Loading State

```javascript
function setButtonLoading(buttonId, isLoading) {
  const button = document.getElementById(buttonId);
  if (!button) return;

  if (isLoading) {
    button.disabled = true;
    button.dataset.originalText = button.textContent;
    button.innerHTML = `
      <span class="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></span>
      Carregando...
    `;
  } else {
    button.disabled = false;
    button.textContent = button.dataset.originalText || 'Salvar';
  }
}

// Usage
setButtonLoading('save-button', true);
// ... API call ...
setButtonLoading('save-button', false);
```

#### Spinner Component

```html
<div class="flex items-center justify-center p-8">
  <div class="relative">
    <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
  </div>
  <p class="ml-4 text-gray-600 dark:text-gray-400">Carregando...</p>
</div>
```

---

## 4. Empty States

### Overview
All list views need empty states when no data is available.

### Implementation

#### Empty State Component

```html
<!-- Generic Empty State -->
<div class="flex flex-col items-center justify-center py-12 px-4 text-center">
  <span class="material-symbols-outlined text-6xl text-gray-400 dark:text-gray-600 mb-4">
    receipt_long
  </span>
  <h3 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">
    Nenhuma transação encontrada
  </h3>
  <p class="text-gray-500 dark:text-gray-400 max-w-md mb-6">
    Você ainda não tem transações cadastradas. Comece adicionando sua primeira transação.
  </p>
  <button
    onclick="openTransactionModal()"
    class="inline-flex items-center gap-2 px-6 py-3 bg-primary text-white rounded-lg font-medium hover:bg-primary/90">
    <span class="material-symbols-outlined">add</span>
    Adicionar Transação
  </button>
</div>

<!-- Empty State with Filter Message -->
<div class="flex flex-col items-center justify-center py-12 px-4 text-center">
  <span class="material-symbols-outlined text-6xl text-gray-400 dark:text-gray-600 mb-4">
    search_off
  </span>
  <h3 class="text-xl font-semibold text-gray-900 dark:text-white mb-2">
    Nenhum resultado encontrado
  </h3>
  <p class="text-gray-500 dark:text-gray-400 max-w-md mb-6">
    Tente ajustar seus filtros de busca para encontrar o que está procurando.
  </p>
  <button
    onclick="resetFilters()"
    class="inline-flex items-center gap-2 px-6 py-3 bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-gray-200 rounded-lg font-medium hover:bg-gray-300 dark:hover:bg-gray-600">
    <span class="material-symbols-outlined">refresh</span>
    Limpar Filtros
  </button>
</div>
```

---

## 5. Success/Error Notifications

### Overview
Toast notification system for user feedback on all actions.

### Implementation

#### Toast Component

```html
<!-- Toast Container -->
<div
  id="toast-container"
  class="fixed top-4 right-4 z-50 space-y-2"
  aria-live="polite"
  aria-atomic="true"></div>
```

#### Toast JavaScript

```javascript
class Toast {
  static show(message, type = 'info', duration = 5000) {
    const container = document.getElementById('toast-container') || this.createContainer();
    const toast = this.createToast(message, type);

    container.appendChild(toast);

    // Animate in
    setTimeout(() => toast.classList.add('toast-enter'), 10);

    // Auto dismiss
    if (duration > 0) {
      setTimeout(() => this.remove(toast), duration);
    }
  }

  static createContainer() {
    const container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'fixed top-4 right-4 z-50 space-y-2';
    container.setAttribute('aria-live', 'polite');
    container.setAttribute('aria-atomic', 'true');
    document.body.appendChild(container);
    return container;
  }

  static createToast(message, type) {
    const toast = document.createElement('div');
    toast.className = `flex items-center gap-3 p-4 rounded-lg shadow-lg bg-white dark:bg-gray-800 min-w-[300px] max-w-[400px] transform translate-x-full transition-transform duration-300`;

    const icons = {
      success: 'check_circle',
      error: 'error',
      warning: 'warning',
      info: 'info'
    };

    const colors = {
      success: 'text-green-600 bg-green-50 dark:bg-green-900/20',
      error: 'text-red-600 bg-red-50 dark:bg-red-900/20',
      warning: 'text-yellow-600 bg-yellow-50 dark:bg-yellow-900/20',
      info: 'text-blue-600 bg-blue-50 dark:bg-blue-900/20'
    };

    toast.innerHTML = `
      <span class="material-symbols-outlined ${colors[type].split(' ')[0]}">${icons[type]}</span>
      <p class="flex-1 text-sm font-medium text-gray-900 dark:text-white">${message}</p>
      <button
        onclick="Toast.remove(this.parentElement)"
        class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300">
        <span class="material-symbols-outlined text-lg">close</span>
      </button>
    `;

    return toast;
  }

  static remove(toast) {
    toast.classList.add('toast-exit');
    setTimeout(() => toast.remove(), 300);
  }

  static success(message) {
    this.show(message, 'success');
  }

  static error(message) {
    this.show(message, 'error');
  }

  static warning(message) {
    this.show(message, 'warning');
  }

  static info(message) {
    this.show(message, 'info');
  }
}

// CSS for animations
const style = document.createElement('style');
style.textContent = `
  .toast-enter {
    transform: translateX(0);
  }

  .toast-exit {
    transform: translateX(calc(100% + 1rem));
    opacity: 0;
  }
`;
document.head.appendChild(style);

// Usage
Toast.success('Transação criada com sucesso!');
Toast.error('Erro ao salvar transação');
Toast.warning('Você tem alterações não salvas');
Toast.info('Dados atualizados');
```

---

## 6. Confirmation Dialogs

### Overview
Destructive actions need confirmation dialogs.

### Implementation

```javascript
class ConfirmationDialog {
  static async show(options = {}) {
    return new Promise((resolve) => {
      const {
        title = 'Confirmar ação',
        message = 'Tem certeza que deseja continuar?',
        confirmText = 'Confirmar',
        cancelText = 'Cancelar',
        variant = 'default' // 'default' | 'danger'
      } = options;

      const dialog = document.createElement('div');
      dialog.className = 'fixed inset-0 z-50 flex items-center justify-center p-4';
      dialog.innerHTML = `
        <div class="fixed inset-0 bg-black bg-opacity-50" onclick="this.parentElement.remove()"></div>
        <div class="relative bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full p-6">
          <h3 class="text-lg font-bold mb-2">${title}</h3>
          <p class="text-gray-600 dark:text-gray-400 mb-6">${message}</p>
          <div class="flex justify-end gap-3">
            <button
              class="px-4 py-2 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
              onclick="this.closest('.fixed').remove(); window.confirmationResult = false;">
              ${cancelText}
            </button>
            <button
              class="px-4 py-2 ${variant === 'danger' ? 'bg-red-600 hover:bg-red-700' : 'bg-primary hover:bg-primary/90'} text-white rounded-lg font-medium"
              onclick="this.closest('.fixed').remove(); window.confirmationResult = true;">
              ${confirmText}
            </button>
          </div>
        </div>
      `;

      document.body.appendChild(dialog);

      // Handle result
      dialog.addEventListener('click', (e) => {
        if (e.target.closest('button')) {
          const result = window.confirmationResult;
          delete window.confirmationResult;
          resolve(result);
        }
      });
    });
  }
}

// Usage
const confirmed = await ConfirmationDialog.show({
  title: 'Excluir transação',
  message: 'Esta ação não pode ser desfeita. Tem certeza que deseja excluir esta transação?',
  variant: 'danger',
  confirmText: 'Excluir',
  cancelText: 'Cancelar'
});

if (confirmed) {
  // Delete transaction
}
```

---

## Testing Checklist

### Filter Sidebar
- [ ] Opens/closes correctly
- [ ] Mobile drawer behavior
- [ ] Focus trap works
- [ ] ESC key closes
- [ ] State persists (desktop)
- [ ] Filter count updates

### Modals
- [ ] Open/close animations
- [ ] Backdrop click closes
- [ ] ESC key closes
- [ ] Focus trap works
- [ ] Unsaved changes warning
- [ ] Loading overlay shows

### Loading States
- [ ] Skeleton loaders appear
- [ ] Button loading states work
- [ ] Spinners animate correctly
- [ ] All async operations show loading

### Empty States
- [ ] Display when no data
- [ ] Correct messaging
- [ ] CTA buttons work
- [ ] Filter reset works

### Notifications
- [ ] Toast appears and animates
- [ ] Auto-dismisses correctly
- [ ] Manual dismiss works
- [ ] Multiple toasts stack
- [ ] Correct colors per type

### Confirmation Dialogs
- [ ] Shows correctly
- [ ] Confirm/cancel work
- [ ] Danger variant styled
- [ ] Click outside closes (optional)

---

**Document Version**: 1.0
**Last Updated**: 2025-01-XX
