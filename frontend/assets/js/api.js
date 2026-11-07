export function listQuery({ page = 1, pageSize = 20, filters = [], orderedBy = [] } = {}) {
  const query = new URLSearchParams({ page, pageSize });
  filters.forEach(filter => query.append('filters', filter));
  orderedBy.forEach(order => query.append('orderedBy', order));
  return query.toString();
}

export function createApi(fetcher = globalThis.fetch) {
  const basePath = new URL('.', document.baseURI).pathname;
  const endpoint = `${basePath.endsWith('/') ? basePath : `${basePath}/`}index.php`;

  async function request(service, path, method = 'GET', body) {
    const [apiPath, rawQuery = ''] = path.split('?');
    const url = new URL(endpoint, document.baseURI);
    url.searchParams.set('proxy', service);
    url.searchParams.set('path', apiPath);
    new URLSearchParams(rawQuery).forEach((value, key) => url.searchParams.append(key, value));

    const usePostOverride = method === 'PATCH' || method === 'DELETE';
    const transportMethod = usePostOverride ? 'POST' : method;
    if (usePostOverride) url.searchParams.set('_method', method);

    const response = await fetcher(`${url.pathname}${url.search}`, {
      method: transportMethod,
      headers: {
        Accept: 'application/json',
        ...(body === undefined ? {} : { 'Content-Type': 'application/json' }),
      },
      ...(body === undefined ? {} : { body: JSON.stringify(body) }),
      signal: AbortSignal.timeout(20000),
    });

    const text = await response.text();
    let data = text || null;
    try { if (text) data = JSON.parse(text); } catch { /* Keep non-JSON error bodies readable. */ }

    if (!response.ok) {
      const error = new Error(data?.message || `Ошибка HTTP ${response.status}`);
      Object.assign(error, { status: response.status, data });
      throw error;
    }

    return { status: response.status, data };
  }

  return {
    list: options => request('cities', `/cities?${listQuery(options)}`),
    create: city => request('cities', '/cities', 'POST', city),
    get: id => request('cities', `/cities/${encodeURIComponent(id)}`),
    update: (id, city) => request('cities', `/cities/${encodeURIComponent(id)}`, 'PATCH', city),
    remove: id => request('cities', `/cities/${encodeURIComponent(id)}`, 'DELETE'),
    special: (parameter, value, type) => request('cities', `/cities/${encodeURIComponent(parameter)}/${encodeURIComponent(value)}?${new URLSearchParams({ type })}`, 'POST'),
    route: target => request('routes', `/route/calculate/${target}`),
  };
}
