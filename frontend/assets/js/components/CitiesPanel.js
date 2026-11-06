import { h, field } from '../dom.js';
import { QueryControls } from './QueryControls.js';
import { CitiesTable } from './CitiesTable.js';

export function CitiesPanel({
  cities, pageData, page, pageSize, busy, filters, setFilters, sorts, setSorts,
  onPageChange, onPageSizeChange, onRefresh, onApplyQuery, onOpenCity, onDeleteCity, onNewCity,
}) {
  return h('section', { className: 'panel', id: 'cities' },
    h('div', { className: 'section-heading' },
      h('h2', null, 'Города'),
      h('button', { type: 'button', onClick: onNewCity }, '+ Новый город')),
    h('div', { className: 'toolbar' },
      field('Страница', h('input', {
        type: 'number', min: 1, value: page,
        onChange: event => onPageChange(Number(event.target.value)),
      })),
      field('На странице', h('input', {
        type: 'number', min: 1, max: 100, value: pageSize,
        onChange: event => onPageSizeChange(Number(event.target.value)),
      })),
      h('button', { type: 'button', onClick: onRefresh }, 'Обновить')),
    h(QueryControls, { filters, setFilters, sorts, setSorts, onApply: onApplyQuery }),
    h('p', { className: 'hint' }, pageData
      ? `Всего городов: ${pageData.totalElements}. На странице: ${cities.length}.`
      : 'Загружаем города…'),
    h(CitiesTable, { cities, onOpen: onOpenCity, onDelete: onDeleteCity }),
    h('div', { className: 'pagination' },
      h('button', {
        type: 'button', className: 'secondary', disabled: page <= 1 || busy,
        onClick: () => onPageChange(page - 1),
      }, '← Назад'),
      h('span', null, `Страница ${pageData?.page ?? page} / ${pageData?.totalPages || 1}`),
      h('button', {
        type: 'button', className: 'secondary',
        disabled: !pageData || page >= pageData.totalPages || busy,
        onClick: () => onPageChange(page + 1),
      }, 'Вперёд →')));
}
