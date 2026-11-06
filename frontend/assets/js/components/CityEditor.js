import { h, field, select } from '../dom.js';
import { CITY_FORM_FIELDS, ENUMS } from '../constants.js';

function FormField({ definition, city, onChange }) {
  const [name, label, type] = definition;
  const required = name !== 'metersAboveSeaLevel';
  return field(label, h('input', {
    type,
    value: city[name] ?? '',
    required,
    step: type === 'number' ? 'any' : undefined,
    onChange: event => onChange(name, event.target.value),
  }), name === 'name' ? 'full' : '');
}

export function CityEditor({
  city, editingId, lookupId, setLookupId, onCityChange, onOpen,
  onSubmit, onReset, onDelete,
}) {
  function updateCityField(name, value) {
    onCityChange(current => ({ ...current, [name]: value }));
  }

  return h('section', { className: 'panel', id: 'editor' },
    h('h2', null, editingId ? `Город №${editingId}` : 'Новый город'),
    h('form', {
      className: 'lookup',
      onSubmit: event => { event.preventDefault(); onOpen(lookupId); },
    },
    field('ID города', h('input', {
      type: 'number', min: 1, required: true, value: lookupId,
      onChange: event => setLookupId(event.target.value),
    })),
    h('button', { type: 'submit', className: 'secondary' }, 'Открыть'),
    h('button', {
      type: 'button', className: 'danger', disabled: !lookupId,
      onClick: () => onDelete(lookupId),
    }, 'Удалить')),
    h('form', { onSubmit },
      h('div', { className: 'form-grid' },
        ...CITY_FORM_FIELDS.map(definition => h(FormField, {
          key: definition[0], definition, city, onChange: updateCityField,
        })),
        field('Климат', select(city.climate, event => updateCityField('climate', event.target.value), ENUMS.climate, undefined, true)),
        field('Форма правления', select(city.government, event => updateCityField('government', event.target.value), ENUMS.government, undefined, true)),
        field('Уровень жизни', select(city.standardOfLiving, event => updateCityField('standardOfLiving', event.target.value), ENUMS.standardOfLiving, 'Не задан'), 'full')),
      h('div', { className: 'actions' },
        h('button', { type: 'submit' }, editingId ? 'Сохранить изменения' : 'Создать город'),
        h('button', { type: 'button', className: 'secondary', onClick: onReset }, 'Новый / сбросить'))));
}
