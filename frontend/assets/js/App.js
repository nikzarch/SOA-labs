import React from 'react';
import { h } from './dom.js';
import { createApi } from './api.js';
import { EMPTY_CITY, INITIAL_FILTER, INITIAL_SORT } from './constants.js';
import { CitiesPanel } from './components/CitiesPanel.js';
import { CityEditor } from './components/CityEditor.js';
import { CityLookup } from './components/CityLookup.js';
import { OperationsPanel } from './components/OperationsPanel.js';
import { DeleteDialog, Toast } from './components/Notifications.js';
import { CityDetailsDialog } from './components/CityDetailsDialog.js';

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
  const [editorMode, setEditorMode] = useState(null);
  const [foundCity, setFoundCity] = useState(null);
  const [lookupId, setLookupId] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [formError, setFormError] = useState('');
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

  async function runAction(action, onError) {
    if (busy) return;
    setBusy(true);
    try {
      return await action();
    } catch (error) {
      if (onError) onError(error);
      else notify(error.message || 'Ошибка запроса');
    } finally {
      setBusy(false);
    }
  }

  function resetEditor() {
    setEditorMode(null);
    setEditingId(null);
    setLookupId('');
    setCity(EMPTY_CITY());
    setFieldErrors({});
    setFormError('');
  }

  async function openCity(id) {
    if (!id) return;
    await runAction(async () => {
      const { data } = await api.get(id);
      setEditingId(data.id);
      setEditorMode('edit');
      setLookupId(data.id);
      setCity(cityToForm(data));
      setFieldErrors({});
      setFormError('');
    });
  }

  async function findCityById(id) {
    if (!id) return;
    await runAction(async () => {
      const { data } = await api.get(id);
      setFoundCity(data);
    });
  }

  function editFoundCity() {
    if (!foundCity) return;
    setEditingId(foundCity.id);
    setLookupId(foundCity.id);
    setCity(cityToForm(foundCity));
    setFieldErrors({});
    setFormError('');
    setFoundCity(null);
    setEditorMode('edit');
  }

  async function saveCity(event) {
    event.preventDefault();
    setFieldErrors({});
    setFormError('');
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
      const result = editorMode === 'edit'
        ? await api.update(editingId, payload)
        : await api.create(payload);
      if (result.data) {
        setEditingId(result.data.id);
        setLookupId(result.data.id);
      }
      await loadCities();
      resetEditor();
      notify(editingId ? 'Изменения сохранены' : 'Город создан');
    }, error => {
      const fieldAliases = {
        'coordinates.x': 'x',
        'coordinates.y': 'y',
        'governor.age': 'age',
      };
      const details = Array.isArray(error.data?.details) ? error.data.details : [];
      const nextErrors = Object.fromEntries(details
        .filter(detail => detail?.field && detail?.message)
        .map(detail => [fieldAliases[detail.field] || detail.field, detail.message]));

      setFieldErrors(nextErrors);
      setFormError(error.data?.message || error.message || 'Не удалось сохранить город');
      if (!Object.keys(nextErrors).length) {
        notify(`HTTP ${error.status || 'ошибка'}: ${error.data?.message || error.message || 'Не удалось сохранить город'}`);
      }
    });
  }

  async function deleteCity() {
    if (deleteTarget === null) return;
    const id = deleteTarget;
    setDeleteTarget(null);

    await runAction(async () => {
      await api.remove(id);
      if (String(foundCity?.id) === String(id)) setFoundCity(null);
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
      setEditorMode('edit');
      setLookupId(result.data.id);
      setCity(cityToForm(result.data));
      setFieldErrors({});
      setFormError('');
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
    setEditingId(null);
    setCity(EMPTY_CITY());
    setFieldErrors({});
    setFormError('');
    setEditorMode('create');
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
      h('div', { className: 'operations-layout' },
        h('section', { className: 'panel lookup-panel' },
          h(CityLookup, { lookupId, setLookupId, onFind: findCityById, onDelete: setDeleteTarget })),
        h(OperationsPanel, {
          search, setSearch, onSearch: searchCity,
          routeResults, onCalculateRoute: calculateRoute,
        })),
      h(CityEditor, {
        city, editingId, editorMode, fieldErrors, formError,
        onCityChange: setCity,
        onClearFieldError: name => {
          setFieldErrors(current => {
            if (!current[name]) return current;
            const next = { ...current };
            delete next[name];
            return next;
          });
          setFormError('');
        },
        onSubmit: saveCity, onReset: resetEditor,
      }),
    ),
    h(Toast, { message: toast }),
    h(DeleteDialog, {
      cityId: deleteTarget, busy,
      onCancel: () => setDeleteTarget(null),
      onConfirm: deleteCity,
    }),
    h(CityDetailsDialog, {
      city: foundCity,
      onClose: () => setFoundCity(null),
      onEdit: editFoundCity,
    }));
}
