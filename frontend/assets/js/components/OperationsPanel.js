import React from 'react';
import { h, field } from '../dom.js';

function CitySearch({ search, setSearch, onSubmit }) {
  return h('form', { onSubmit },
    h('h3', null, 'Город по условию'),
    h('div', { className: 'form-grid' },
      field('Параметр', h('select', {
        value: search.parameter,
        onChange: event => setSearch(current => ({ ...current, parameter: event.target.value })),
      },
      h('option', { value: 'name' }, 'Название'),
      h('option', { value: 'area' }, 'Площадь'),
      h('option', { value: 'standard-of-living' }, 'Уровень жизни'))),
      field('Операция', h('select', {
        value: search.type,
        onChange: event => setSearch(current => ({ ...current, type: event.target.value })),
      },
      h('option', { value: 'EQUALS' }, 'Равно'),
      h('option', { value: 'MIN' }, 'Минимум'),
      h('option', { value: 'MAX' }, 'Максимум'))),
      field('Значение', h('input', {
        required: true,
        value: search.value,
        onChange: event => setSearch(current => ({ ...current, value: event.target.value })),
      }), 'full')),
    h('button', { type: 'submit', className: 'secondary search-submit' }, 'Найти город'));
}

function RouteCalculator({ results, onCalculate }) {
  const routes = [
    ['to-largest', 'До крупнейшего'],
    ['to-newest', 'До новейшего'],
  ];

  return h('div', { className: 'route-section' },
    h('h3', null, 'Расчёт маршрута'),
    ...routes.map(([route, title]) => h(React.Fragment, { key: route },
      h('div', { className: 'route-card' },
        h('strong', null, title),
        h('button', { type: 'button', className: 'secondary', onClick: () => onCalculate(route) }, 'Рассчитать')),
      h('output', { className: 'distance' }, results[route] || 'Ещё не рассчитан'))));
}

export function OperationsPanel({ search, setSearch, onSearch, routeResults, onCalculateRoute }) {
  return h('section', { className: 'panel', id: 'operations' },
    h('h2', null, 'Поиск и маршруты'),
    h(CitySearch, { search, setSearch, onSubmit: onSearch }),
    h(RouteCalculator, { results: routeResults, onCalculate: onCalculateRoute }));
}
