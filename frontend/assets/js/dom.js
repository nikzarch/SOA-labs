import React from 'react';

export const h = React.createElement;

export function field(label, control, className = '') {
  return h('label', { className }, label, control);
}

export function select(value, onChange, values, placeholder = 'Выберите…', required = false) {
  return h(
    'select',
    { value, onChange, required },
    h('option', { value: '' }, placeholder),
    ...values.map(item => {
      const [optionValue, label] = Array.isArray(item) ? item : [item, item];
      return h('option', { key: optionValue, value: optionValue }, label);
    }),
  );
}
