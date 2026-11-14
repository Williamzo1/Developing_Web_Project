import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useApi, useMutation } from './api/hooks';
import { useAuth } from './auth/AuthContext';
import DataState from './components/DataState';
import styles from './components/Form.module.css';

import { MapView } from './WorkshopMap';

const inputs = [
  ['name', 'Tên xưởng', 'text', 160], ['address', 'Địa chỉ', 'text', 300],
  ['phone', 'Số điện thoại', 'tel', 40], ['contactEmail', 'Email liên hệ (Gmail)', 'email', 254],
  ['facebookUrl', 'Facebook', 'url', 500],
  ['description', 'Giới thiệu', 'textarea', 10000], ['familyHistory', 'Lịch sử làm nghề', 'textarea', 10000],
];

export default function WorkshopEdit() {
  const path = '/api/artisan/workshop';
  const { token } = useAuth();
  const { current, retry } = useApi(path, token);
  const [coordinates, setCoordinates] = useState(null);
  const [saved, setSaved] = useState(false);
  const mutation = useMutation(token);
  const profile = current?.data;
  if (!profile) return <DataState current={current} retry={retry} missing="Không tìm thấy hồ sơ xưởng." />;

  const latitude = coordinates ? coordinates[0] : profile.latitude ?? '';
  const longitude = coordinates ? coordinates[1] : profile.longitude ?? '';
  const hasCoordinates = latitude !== '' && longitude !== '';

  async function save(event) {
    event.preventDefault();
    setSaved(false);
    const body = Object.fromEntries(new FormData(event.currentTarget));
    for (const field of ['latitude', 'longitude']) body[field] = body[field] === '' ? null : Number(body[field]);
    setSaved(await mutation.submit(path, 'PUT', body));
  }

  return <section>
    <h1>Hồ sơ xưởng</h1>
    <p><Link to={'/ho-gia-dinh/' + encodeURIComponent(profile.slug)}>Xem hồ sơ công khai</Link></p>
    <form className={styles.form} onSubmit={save} onChange={() => setSaved(false)}>
      {inputs.map(([name, label, type, limit]) => <label key={name}>{label}
        {type === 'textarea' ? <textarea name={name} defaultValue={profile[name] ?? ''} rows={4} maxLength={limit} /> :
          <input name={name} type={type} defaultValue={profile[name] ?? ''} required={name === 'name'}
            maxLength={limit} />}
        {mutation.error?.fieldErrors?.[name] && <small>{mutation.error.fieldErrors[name]}</small>}
      </label>)}
      <label>Vĩ độ<input type="number" name="latitude" step="any" min="-90" max="90" value={latitude}
        onChange={event => setCoordinates([event.target.value, longitude])} /></label>
      <label>Kinh độ<input type="number" name="longitude" step="any" min="-180" max="180" value={longitude}
        onChange={event => setCoordinates([latitude, event.target.value])} /></label>
      <p>Chọn vị trí trên bản đồ hoặc nhập tọa độ.</p>
      <MapView initial={profile.latitude == null ? undefined : [profile.latitude, profile.longitude]}
        points={hasCoordinates ? [{ slug: '', name: 'Vị trí xưởng', latitude: Number(latitude), longitude: Number(longitude) }] : []}
        onPick={point => { setCoordinates(point); setSaved(false); }} />
      {mutation.error && <p role="alert">{mutation.error.fieldErrors?.coordinatePairValid || mutation.error.message}</p>}
      {saved && <p role="status">Đã lưu hồ sơ xưởng.</p>}
      <button className="button" disabled={mutation.pending}>Lưu hồ sơ</button>
    </form>
  </section>;
}
