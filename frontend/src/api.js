const BASE_URL = '';

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
