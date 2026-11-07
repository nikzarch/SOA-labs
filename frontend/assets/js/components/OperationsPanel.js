import React from 'react';
import { h, field } from '../dom.js';

function CitySearch({ search, setSearch, onSubmit }) {
  return h('form', { onSubmit },
    h('h3', null, '\u0413\u043e\u0440\u043e\u0434 \u043f\u043e \u0443\u0441\u043b\u043e\u0432\u0438\u044e'),
    h('div', { className: 'form-grid' },
      field('\u041f\u0430\u0440\u0430\u043c\u0435\u0442\u0440', h('select', {
        value: search.parameter,
        onChange: event => setSearch(current => ({ ...current, parameter: event.target.value })),
      },
      h('option', { value: 'name' }, '\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435'),
      h('option', { value: 'area' }, '\u041f\u043b\u043e\u0449\u0430\u0434\u044c'),
      h('option', { value: 'standard-of-living' }, '\u0423\u0440\u043e\u0432\u0435\u043d\u044c \u0436\u0438\u0437\u043d\u0438'))),
      field('\u041e\u043f\u0435\u0440\u0430\u0446\u0438\u044f', h('select', {
        value: search.type,
        onChange: event => setSearch(current => ({ ...current, type: event.target.value })),
      },
      h('option', { value: 'EQUALS' }, '\u0420\u0430\u0432\u043d\u043e'),
      h('option', { value: 'MIN' }, '\u041c\u0438\u043d\u0438\u043c\u0443\u043c'),
      h('option', { value: 'MAX' }, '\u041c\u0430\u043a\u0441\u0438\u043c\u0443\u043c'))),
      field('\u0417\u043d\u0430\u0447\u0435\u043d\u0438\u0435', h('input', {
        required: true,
        value: search.value,
        onChange: event => setSearch(current => ({ ...current, value: event.target.value })),
      }), 'full')),
    h('button', { type: 'submit', className: 'secondary search-submit' }, '\u041d\u0430\u0439\u0442\u0438 \u0433\u043e\u0440\u043e\u0434'));
}

function RouteCalculator({ results, onCalculate }) {
  const routes = [
    ['to-largest', '\u041e\u0442 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b 0 \u0434\u043e \u043a\u0440\u0443\u043f\u043d\u0435\u0439\u0448\u0435\u0433\u043e \u0433\u043e\u0440\u043e\u0434\u0430'],
    ['to-newest', '\u041e\u0442 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b 0 \u0434\u043e \u043d\u043e\u0432\u0435\u0439\u0448\u0435\u0433\u043e \u0433\u043e\u0440\u043e\u0434\u0430'],
  ];

  return h('div', { className: 'route-section' },
    h('h3', null, '\u0420\u0430\u0441\u0447\u0451\u0442 \u043c\u0430\u0440\u0448\u0440\u0443\u0442\u0430'),
    ...routes.map(([route, title]) => h(React.Fragment, { key: route },
      h('div', { className: 'route-card' },
        h('strong', null, title),
        h('button', { type: 'button', className: 'secondary', onClick: () => onCalculate(route) }, '\u0420\u0430\u0441\u0441\u0447\u0438\u0442\u0430\u0442\u044c'),
      ),
      h('output', { className: 'distance' }, results[route] || '\u0415\u0449\u0451 \u043d\u0435 \u0440\u0430\u0441\u0441\u0447\u0438\u0442\u0430\u043d'),
    )),
  );
}

export function OperationsPanel({ search, setSearch, onSearch, routeResults, onCalculateRoute }) {
  return h('section', { className: 'panel', id: 'operations' },
    h('h2', null, '\u041f\u043e\u0438\u0441\u043a \u0438 \u043c\u0430\u0440\u0448\u0440\u0443\u0442\u044b'),
    h(CitySearch, { search, setSearch, onSubmit: onSearch }),
    h(RouteCalculator, { results: routeResults, onCalculate: onCalculateRoute }));
}
