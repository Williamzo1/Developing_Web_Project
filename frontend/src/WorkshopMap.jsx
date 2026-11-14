import React, { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { useApi } from './api/hooks';
import DataState from './components/DataState';
import styles from './WorkshopMap.module.css';

const tileSize = 256;
const initialCenter = [20.977, 105.914];
const maxLatitude = 85.05112878;

function project(latitude, longitude, zoom) {
  const size = tileSize * 2 ** zoom;
  const radians = Math.max(-maxLatitude, Math.min(maxLatitude, latitude)) * Math.PI / 180;
  return [(longitude + 180) / 360 * size, (1 - Math.asinh(Math.tan(radians)) / Math.PI) * size / 2];
}

function unproject(x, y, zoom) {
  const size = tileSize * 2 ** zoom;
  return [Math.atan(Math.sinh(Math.PI * (1 - 2 * y / size))) * 180 / Math.PI, x / size * 360 - 180];
}

export function MapView({ points, onPick, initial = initialCenter }) {
  const frame = useRef(null);
  const drag = useRef(null);
  const [center, setCenter] = useState(initial);
  const [zoom, setZoom] = useState(15);
  const [active, setActive] = useState(null);
  const [tileFailed, setTileFailed] = useState(false);
  const [dimensions, setDimensions] = useState(null);
  const [cx, cy] = project(...center, zoom);
  const tileCount = 2 ** zoom;
  const tiles = [];
  function centerAt(x, y) {
    const size = tileSize * tileCount;
    setCenter(unproject(Math.min(size - 1, Math.max(0, x)), Math.min(size - 1, Math.max(0, y)), zoom));
  }

  useEffect(() => {
    const observer = new ResizeObserver(([entry]) => {
      setDimensions([entry.contentRect.width, entry.contentRect.height]);
    });
    observer.observe(frame.current);
    return () => observer.disconnect();
  }, []);

  const halfWidth = (dimensions?.[0] ?? 0) / 2;
  const halfHeight = (dimensions?.[1] ?? 0) / 2;
  for (let row = Math.floor((cy - halfHeight) / tileSize); dimensions && row <= Math.floor((cy + halfHeight) / tileSize); row++) {
    for (let col = Math.floor((cx - halfWidth) / tileSize); col <= Math.floor((cx + halfWidth) / tileSize); col++) {
      if (row < 0 || row >= tileCount || col < 0 || col >= tileCount) continue;
      const x = col * tileSize - cx;
      const y = row * tileSize - cy;
      tiles.push(<img key={zoom + '/' + col + '/' + row} className={styles.tile} alt="" width={tileSize} height={tileSize}
        draggable={false} referrerPolicy="strict-origin"
        style={{ left: x, top: y }} onError={() => setTileFailed(true)}
        src={'https://tile.openstreetmap.org/' + zoom + '/' + col + '/' + row + '.png'} />);
    }
  }

  function finish(event) {
    const beginning = drag.current;
    drag.current = null;
    if (!beginning || beginning.pointerId !== event.pointerId) return;
    const dx = event.clientX - beginning.x;
    const dy = event.clientY - beginning.y;
    if (Math.hypot(dx, dy) >= 5) centerAt(cx - dx, cy - dy);
    else if (onPick) {
      const rect = frame.current.getBoundingClientRect();
      const coordinates = unproject(cx + event.clientX - rect.left - rect.width / 2,
        cy + event.clientY - rect.top - rect.height / 2, zoom);
      onPick(coordinates.map(value => Number(value.toFixed(6))));
    }
  }

  return <div className={styles.map}>
    <div className={styles.toolbar} aria-label="Điều khiển bản đồ">
      <button type="button" disabled={zoom >= 19} onClick={() => setZoom(zoom + 1)}>Phóng to</button>
      <button type="button" disabled={zoom <= 4} onClick={() => setZoom(zoom - 1)}>Thu nhỏ</button>
      <button type="button" aria-label="Dịch sang trái" onClick={() => centerAt(cx - 160, cy)}>←</button>
      <button type="button" aria-label="Dịch lên trên" onClick={() => centerAt(cx, cy - 160)}>↑</button>
      <button type="button" aria-label="Dịch xuống dưới" onClick={() => centerAt(cx, cy + 160)}>↓</button>
      <button type="button" aria-label="Dịch sang phải" onClick={() => centerAt(cx + 160, cy)}>→</button>
      <button type="button" onClick={() => { setCenter(initial); setZoom(15); setActive(null); }}>Về vị trí ban đầu</button>
    </div>
    <div ref={frame} className={styles.frame} aria-label="Bản đồ xưởng Bát Tràng"
      onPointerDown={event => {
        if (event.button !== 0 || event.target.closest('button, a')) return;
        drag.current = { pointerId: event.pointerId, x: event.clientX, y: event.clientY };
        event.currentTarget.setPointerCapture(event.pointerId);
      }} onPointerUp={finish} onPointerCancel={() => { drag.current = null; }}>
      <div className={styles.origin}>{tiles}
        {points.map(point => {
          const [px, py] = project(point.latitude, point.longitude, zoom);
          const x = px - cx, y = py - cy;
          const visible = Math.abs(x) < halfWidth - 18 && Math.abs(y) < halfHeight - 18;
          return visible && <button key={point.slug} className={styles.pin} type="button"
            style={{ left: x, top: y }} aria-label={point.name} aria-pressed={active?.slug === point.slug}
            onClick={() => setActive(point)}>●</button>;
        })}
      </div>
    </div>
    <a className={styles.credit} href="https://www.openstreetmap.org/copyright" rel="noreferrer">© OpenStreetMap</a>
    {tileFailed && <p role="status">Một số ô bản đồ chưa tải được.</p>}
    {active && <article className="notice"><h2>{active.name}</h2>
      {active.description && <p className="preserve-lines">{active.description}</p>}
      {active.slug && <Link to={'/ho-gia-dinh/' + encodeURIComponent(active.slug)}>Xem hồ sơ xưởng</Link>}
    </article>}
  </div>;
}

export default function WorkshopMap() {
  const result = useApi('/api/map/workshops');
  const pins = result.current?.data;
  const [selection, setSelection] = useState(null);
  return <section>
    <h1>Bản đồ làng nghề</h1>
    {!pins ? <DataState {...result} /> : <>
      <MapView key={selection?.slug || 'default'} points={pins}
        initial={selection ? [selection.latitude, selection.longitude] : initialCenter} />
      {pins.length === 0 ? <p className="notice">Chưa có xưởng công bố tọa độ.</p> :
        <ul className="grid">{pins.map(pin => <li key={pin.slug} className="card card-copy">
          <button className="button" type="button" onClick={() => setSelection(pin)}>{pin.name}</button>
        </li>)}</ul>}
    </>}
  </section>;
}
