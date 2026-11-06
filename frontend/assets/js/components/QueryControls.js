import { h, select } from '../dom.js';
import { CITY_QUERY_FIELDS } from '../constants.js';

const FILTER_OPERATORS = ['equals', 'moreThan', 'lessThan'];
const SORT_DIRECTIONS = [
  ['asc', 'По возрастанию'],
  ['desc', 'По убыванию'],
];

function QueryRow({ children, onRemove, removeLabel }) {
  return h('div', { className: 'query-row' }, children,
    onRemove && h('button', {
      type: 'button', className: 'secondary compact', onClick: onRemove,
      'aria-label': removeLabel,
    }, '×'));
}

function FilterGroup({ filters, setFilters }) {
  function updateFilter(index, key, value) {
    setFilters(items => items.map((item, i) => i === index
      ? { ...item, [key]: value, ...(key === 'field' ? { operator: 'equals' } : {}) }
      : item));
  }

  return h('section', { className: 'query-group', 'aria-labelledby': 'filters-title' },
    h('h3', { id: 'filters-title' }, 'Фильтры'),
    ...filters.map((filter, index) => h(QueryRow, {
      key: `filter-${index}`,
      onRemove: filters.length > 1 ? () => setFilters(items => items.filter((_, i) => i !== index)) : null,
      removeLabel: 'Удалить фильтр',
    },
    select(filter.field, event => updateFilter(index, 'field', event.target.value), CITY_QUERY_FIELDS, 'Поле'),
    select(filter.operator, event => updateFilter(index, 'operator', event.target.value), FILTER_OPERATORS),
    h('input', {
      value: filter.value,
      placeholder: 'Значение',
      'aria-label': 'Значение фильтра',
      onChange: event => updateFilter(index, 'value', event.target.value),
    }))),
    h('button', {
      type: 'button', className: 'secondary',
      onClick: () => setFilters(items => [...items, { field: 'name', operator: 'equals', value: '' }]),
    }, '+ Фильтр'));
}

function SortGroup({ sorts, setSorts }) {
  function updateSort(index, key, value) {
    setSorts(items => items.map((item, i) => i === index ? { ...item, [key]: value } : item));
  }

  return h('section', { className: 'query-group sort-group', 'aria-labelledby': 'sort-title' },
    h('h3', { id: 'sort-title' }, 'Сортировка'),
    ...sorts.map((sort, index) => h(QueryRow, {
      key: `sort-${index}`,
      onRemove: sorts.length > 1 ? () => setSorts(items => items.filter((_, i) => i !== index)) : null,
      removeLabel: 'Удалить сортировку',
    },
    select(sort.field, event => updateSort(index, 'field', event.target.value), CITY_QUERY_FIELDS, 'Поле'),
    select(sort.direction, event => updateSort(index, 'direction', event.target.value), SORT_DIRECTIONS))),
    h('button', {
      type: 'button', className: 'secondary',
      onClick: () => setSorts(items => [...items, { field: 'area', direction: 'desc' }]),
    }, '+ Сортировка'));
}

export function QueryControls({ filters, setFilters, sorts, setSorts, onApply }) {
  return h('div', { className: 'query-controls' },
    h(FilterGroup, { filters, setFilters }),
    h(SortGroup, { sorts, setSorts }),
    h('button', { type: 'button', onClick: onApply }, 'Применить'));
}
