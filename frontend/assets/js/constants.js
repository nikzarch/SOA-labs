export const ENUMS = {
  climate: ['RAIN_FOREST', 'HUMIDSUBTROPICAL', 'HUMIDCONTINENTAL', 'SUBARCTIC', 'DESERT'],
  government: ['DICTATORSHIP', 'KLEPTOCRACY', 'KRITARCHY', 'REPUBLIC', 'TELLUROCRACY'],
  standardOfLiving: ['ULTRA_HIGH', 'HIGH', 'NIGHTMARE'],
};

export const CITY_QUERY_FIELDS = [
  ['id', 'ID'],
  ['name', 'Название'],
  ['coordinates.x', 'Координата X'],
  ['coordinates.y', 'Координата Y'],
  ['creationDate', 'Дата создания'],
  ['area', 'Площадь'],
  ['population', 'Население'],
  ['metersAboveSeaLevel', 'Высота'],
  ['climate', 'Климат'],
  ['government', 'Правление'],
  ['standardOfLiving', 'Уровень жизни'],
  ['governor.age', 'Возраст губернатора'],
];

export const CITY_FORM_FIELDS = [
  ['name', 'Название', 'text'],
  ['x', 'Координата X', 'number'],
  ['y', 'Координата Y', 'number'],
  ['area', 'Площадь', 'number'],
  ['population', 'Население', 'number'],
  ['metersAboveSeaLevel', 'Высота над уровнем моря', 'number'],
  ['age', 'Возраст губернатора', 'number'],
];

export const EMPTY_CITY = () => ({
  name: '', x: 0, y: '', area: '', population: '', metersAboveSeaLevel: '',
  age: '', climate: '', government: '', standardOfLiving: '',
});

export const INITIAL_FILTER = () => ({ field: 'name', operator: 'equals', value: '' });
export const INITIAL_SORT = () => ({ field: 'area', direction: 'desc' });
