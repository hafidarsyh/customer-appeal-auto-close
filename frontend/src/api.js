const BASE_URL = 'http://localhost:8090';

export async function fetchAppeals() {
  const response = await fetch(`${BASE_URL}/api/staff/appeals`);
  if (!response.ok) {
    throw new Error(`Failed to fetch appeals list (${response.status})`);
  }
  return response.json();
}

export async function fetchAppealDetail(id) {
  const response = await fetch(`${BASE_URL}/api/staff/appeals/${id}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch appeal details (${response.status})`);
  }
  return response.json();
}

export async function respondToAppeal(id, responseText) {
  const response = await fetch(`${BASE_URL}/api/staff/appeals/${id}/respond`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ response: responseText }),
  });
  if (!response.ok) {
    if (response.status === 409) {
      throw new Error('Action forbidden: Appeal is already CLOSED');
    }
    throw new Error(`Failed to submit response (${response.status})`);
  }
  return response.json();
}

export async function submitAppeal(appealData) {
  const response = await fetch(`${BASE_URL}/appeals`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(appealData),
  });

  if (!response.ok) {
    if (response.status === 400) {
      const errorData = await response.json();
      throw { isValidationError: true, errors: errorData };
    }
    throw new Error(`Failed to submit appeal (${response.status})`);
  }
  return response.json();
}

export async function manualSync() {
  const response = await fetch(`${BASE_URL}/api/staff/sync`, {
    method: 'POST',
  });
  if (!response.ok) {
    throw new Error(`Failed to trigger manual sync (${response.status})`);
  }
  return response.json();
}
