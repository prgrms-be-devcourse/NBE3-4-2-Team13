'use client';

import { useEffect, useRef } from 'react';

interface KakaoMapProps {
  latitude: string;
  longitude: string;
  level?: number;
}

declare global {
  interface Window {
    kakao: any;
  }
}

export default function KakaoMap({ latitude, longitude, level = 7 }: KakaoMapProps) {
  const mapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const script = document.createElement('script');
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_MAP_KEY}&autoload=false`;

    script.onload = () => {
      window.kakao.maps.load(() => {
        if (!mapRef.current) return;

        try {
          const coords = new window.kakao.maps.LatLng(latitude, longitude);
          const options = {
            center: coords,
            level: level,
          };

          const map = new window.kakao.maps.Map(mapRef.current, options);

          // 지도 타입 컨트롤
          const mapTypeControl = new window.kakao.maps.MapTypeControl();
          map.addControl(mapTypeControl, window.kakao.maps.ControlPosition.TOPRIGHT);

          // 줌 컨트롤
          const zoomControl = new window.kakao.maps.ZoomControl();
          map.addControl(zoomControl, window.kakao.maps.ControlPosition.RIGHT);
        } catch (error) {
          console.error('Error creating map:', error);
        }
      });
    };

    document.head.appendChild(script);

    return () => {
      if (document.head.contains(script)) {
        document.head.removeChild(script);
      }
    };
  }, [latitude, longitude, level]);

  return (
    <div ref={mapRef} className='w-full h-[400px] rounded-lg shadow-lg mb-8' style={{ border: '1px solid #e2e8f0' }} />
  );
}
