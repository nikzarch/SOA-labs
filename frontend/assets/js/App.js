import React from 'react';
import { h } from './dom.js';
import { createApi } from './api.js';
import { EMPTY_CITY, INITIAL_FILTER, INITIAL_SORT } from './constants.js';
import { CitiesPanel } from './components/CitiesPanel.js';
import { CityEditor } from './components/CityEditor.js';
import { OperationsPanel } from './components/OperationsPanel.js';
import { DeleteDialog, Toast } from './components/Notifications.js';

const { useEffect, useState } = React;
const api = createApi();

function cityToForm(city) {
  return {
    ...EMPTY_CITY(),
    ...city,
    x: city.coordinates?.x ?? '',
    y: city.coordinates?.y ?? '',
    age: city.governor?.age ?? '',
  };
}

export function App() {
  const [cities, setCities] = useState([]);
  const [pageData, setPageData] = useState(null);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [filters, setFilters] = useState([INITIAL_FILTER()]);
  const [sorts, setSorts] = useState([INITIAL_SORT()]);

  const [city, setCity] = useState(EMPTY_CITY());
  const [editingId, setEditingId] = useState(null);
  const [lookupId, setLookupId] = useState('');
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [busy, setBusy] = useState(false);
  const [toast, setToast] = useState('');

  const [search, setSearch] = useState({ parameter: 'name', type: 'EQUALS', value: '' });
  const [routeResults, setRouteResults] = useState({});

  function notify(message) {
    setToast(message);
    window.setTimeout(() => setToast(''), 4000);
  }

  async function loadCities({ nextPage = page } = {}) {
    const activeFilters = filters
      .filter(filter => filter.value.trim())
      .map(filter => `${filter.field}:${filter.operator}:${filter.value}`);
    const orderedBy = sorts
      .filter(sort => sort.field)
      .map(sort => `${sort.field}:${sort.direction}`);
    const { data } = await api.list({ page: nextPage, pageSize, filters: activeFilters, orderedBy });

    setCities(Array.isArray(data.content) ? data.content : []);
    setPageData(data);
    setPage(data.page || nextPage);
    return data;
  }

  useEffect(() => {
    loadCities().catch(error => notify(`Не удалось загрузить города: ${error.message}`));
  }, []);

  useEffect(() => {
    const timer = window.setInterval(() => {
      if (!document.hidden) loadCities().catch(() => {});
    }, 5000);
    return () => window.clearInterval(timer);
  }, [page, pageSize, filters, sorts]);

  async function runAction(action) {
    if (busy) return;
    setBusy(true);
    try {
      return await action();
    } catch (error) {
      notify(error.message || 'Ошибка запроса');
    } finally {
      setBusy(false);
    }
  }

  function resetEditor() {
    setEditingId(null);
    setLookupId('');
    setCity(EMPTY_CITY());
  }

  async function openCity(id) {
    if (!id) return;
    await runAction(async () => {
      const { data } = await api.get(id);
      setEditingId(data.id);
      setLookupId(data.id);
      setCity(cityToForm(data));
      document.getElementById('editor')?.scrollIntoView({ behavior: 'smooth' });
    });
  }

  async function saveCity(event) {
    event.preventDefault();
    const payload = {
      name: city.name.trim(),
      coordinates: { x: Number(city.x), y: Number(city.y) },
      area: Number(city.area),
      population: Number(city.population),
      metersAboveSeaLevel: city.metersAboveSeaLevel === '' ? null : Number(city.metersAboveSeaLevel),
      climate: city.climate,
      government: city.government,
      standardOfLiving: city.standardOfLiving || null,
      governor: { age: Number(city.age) },
    };

    await runAction(async () => {
      const result = editingId
        ? await api.update(editingId, payload)
        : await api.create(payload);
      if (result.data) {
        setEditingId(result.data.id);
        setLookupId(result.data.id);
      }
      await loadCities();
      notify(editingId ? 'Изменения сохранены' : 'Город создан');
    });
  }

  async function deleteCity() {
    if (deleteTarget === null) return;
    const id = deleteTarget;
    setDeleteTarget(null);

    await runAction(async () => {
      await api.remove(id);
      if (String(editingId) === String(id)) resetEditor();
      await loadCities();
      notify('Город удалён');
    });
  }

  async function searchCity(event) {
    event.preventDefault();
    await runAction(async () => {
      let result;
      try {
        result = await api.special(search.parameter, search.value, search.type);
      } catch (error) {
        if (error.status === 404) { notify('Город не найден'); return; }
        throw error;
      }

      if (!result.data) { notify('Город не найден'); return; }
      setEditingId(result.data.id);
      setLookupId(result.data.id);
      setCity(cityToForm(result.data));
    });
  }

  async function calculateRoute(route) {
    await runAction(async () => {
      setRouteResults(current => ({ ...current, [route]: 'Выполняется расчёт…' }));
      const { data } = await api.route(route);
      setRouteResults(current => ({
        ...current,
        [route]: `Длина маршрута: ${Number(data.length).toLocaleString('ru-RU', { maximumFractionDigits: 6 })}`,
      }));
    });
  }

  function startNewCity() {
    resetEditor();
    document.getElementById('editor')?.scrollIntoView({ behavior: 'smooth' });
  }

  async function changePage(nextPage) {
    setPage(nextPage);
    await runAction(() => loadCities({ nextPage }));
  }

  async function applyQuery() {
    setPage(1);
    await runAction(() => loadCities({ nextPage: 1 }));
  }

  return h(React.Fragment, null,
    h('main', null,
      h(CitiesPanel, {
        cities, pageData, page, pageSize, busy, filters, setFilters, sorts, setSorts,
        onPageChange: changePage,
        onPageSizeChange: setPageSize,
        onRefresh: () => runAction(() => loadCities()),
        onApplyQuery: applyQuery,
        onOpenCity: openCity,
        onDeleteCity: setDeleteTarget,
        onNewCity: startNewCity,
      }),
      h('div', { className: 'columns' },
        h(CityEditor, {
          city, editingId, lookupId, setLookupId, onCityChange: setCity,
          onOpen: openCity, onSubmit: saveCity, onReset: resetEditor,
          onDelete: setDeleteTarget,
        }),
        h(OperationsPanel, {
          search, setSearch, onSearch: searchCity,
          routeResults, onCalculateRoute: calculateRoute,
        }))),
    h(Toast, { message: toast }),
    h(DeleteDialog, {
      cityId: deleteTarget, busy,
      onCancel: () => setDeleteTarget(null),
      onConfirm: deleteCity,
    }));
}
