import React from 'react';
import { h, field, select } from '../dom.js';
import { CITY_FORM_FIELDS, ENUMS } from '../constants.js';

function fieldWithError(label, control, name, errors, className = '') {
  const message = errors[name];
  return field(label, h(React.Fragment, null,
    React.cloneElement(control, {
      'aria-invalid': Boolean(message),
      'aria-describedby': message ? `error-${name}` : undefined,
    }),
    message && h('span', { className: 'field-error', id: `error-${name}` }, message)),
  message ? `${className} field-invalid`.trim() : className);
}

function FormField({ definition, city, errors, onChange }) {
  const [name, label, type] = definition;
  const required = name !== 'metersAboveSeaLevel';
  return fieldWithError(label, h('input', {
    type,
    value: city[name] ?? '',
    required,
    step: type === 'number' ? 'any' : undefined,
    onChange: event => onChange(name, event.target.value),
  }), name, errors, name === 'name' ? 'full' : '');
}

export function CityEditor({ city, editingId, editorMode, fieldErrors, formError,
  onCityChange, onClearFieldError, onSubmit, onReset }) {
  if (!editorMode) return null;

  function updateCityField(name, value) {
    onCityChange(current => ({ ...current, [name]: value }));
    onClearFieldError(name);
  }

  const editing = editorMode === 'edit';
  return h('div', {
    className: 'modal-backdrop editor-backdrop',
    onMouseDown: event => { if (event.target === event.currentTarget) onReset(); },
  },
  h('section', { className: 'modal editor-modal', role: 'dialog', 'aria-modal': 'true', 'aria-labelledby': 'editor-title' },
    h('div', { className: 'section-heading' },
      h('h2', { id: 'editor-title' }, editing ? `\u0413\u043e\u0440\u043e\u0434 \u2116${editingId}` : '\u041d\u043e\u0432\u044b\u0439 \u0433\u043e\u0440\u043e\u0434'),
      h('button', { type: 'button', className: 'icon-button', onClick: onReset, 'aria-label': 'Close' }, '\u00d7')),
    h('div', { className: 'city-form-heading' },
      h('h3', null, editing ? '\u0418\u0437\u043c\u0435\u043d\u0438\u0442\u044c \u0434\u0430\u043d\u043d\u044b\u0435 \u0433\u043e\u0440\u043e\u0434\u0430' : '\u0414\u0430\u043d\u043d\u044b\u0435 \u043d\u043e\u0432\u043e\u0433\u043e \u0433\u043e\u0440\u043e\u0434\u0430')),
    formError && h('p', { className: 'form-error', role: 'alert' }, formError),
    h('form', { onSubmit },
      h('div', { className: 'form-grid' },
        ...CITY_FORM_FIELDS.map(definition => h(FormField, {
          key: definition[0], definition, city, errors: fieldErrors, onChange: updateCityField,
        })),
        fieldWithError('\u041a\u043b\u0438\u043c\u0430\u0442', select(city.climate, event => updateCityField('climate', event.target.value), ENUMS.climate, undefined, true), 'climate', fieldErrors, 'climate-field'),
        fieldWithError('\u0424\u043e\u0440\u043c\u0430 \u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f', select(city.government, event => updateCityField('government', event.target.value), ENUMS.government, undefined, true), 'government', fieldErrors),
        fieldWithError('\u0423\u0440\u043e\u0432\u0435\u043d\u044c \u0436\u0438\u0437\u043d\u0438', select(city.standardOfLiving, event => updateCityField('standardOfLiving', event.target.value), ENUMS.standardOfLiving, '\u041d\u0435 \u0437\u0430\u0434\u0430\u043d'), 'standardOfLiving', fieldErrors, 'full')),
      h('div', { className: 'actions' },
        h('button', { type: 'submit' }, editing ? '\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0438\u044f' : '\u0421\u043e\u0437\u0434\u0430\u0442\u044c \u0433\u043e\u0440\u043e\u0434'),
        h('button', { type: 'button', className: 'secondary', onClick: onReset }, '\u041e\u0442\u043c\u0435\u043d\u0430')))));
}
