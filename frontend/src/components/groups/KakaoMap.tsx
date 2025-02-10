'use client';

import { useEffect, useRef } from 'react';

interface KakaoMapProps {
  latitude: string;
  longitude: string;
  level: number;
  groupName?: string;
  address?: string;
}

declare global {
  interface Window {
    kakao: any;
  }
}

export default function KakaoMap({ latitude, longitude, level, groupName, address }: KakaoMapProps) {
  const mapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const script = document.createElement('script');
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_MAP_KEY}&libraries=services&autoload=false`;

    script.onload = () => {
      window.kakao.maps.load(() => {
        if (!mapRef.current) return;

        const position = new window.kakao.maps.LatLng(parseFloat(latitude), parseFloat(longitude));
        const options = {
          center: position,
          level: level,
        };

        const map = new window.kakao.maps.Map(mapRef.current, options);

        // 커스텀 오버레이 콘텐츠
        const content = `
          <div class="custom-overlay bg-white dark:bg-gray-800 p-3 rounded-lg shadow-lg">
            <h3 class="font-bold text-gray-900 dark:text-white">${groupName || '모임 위치'}</h3>
            <p class="text-sm text-gray-600 dark:text-gray-300">${address || ''}</p>
          </div>
        `;

        // 커스텀 오버레이 생성
        new window.kakao.maps.CustomOverlay({
          map: map,
          position: position,
          content: content,
          yAnchor: 1,
        });

        // 지도 컨트롤 추가
        const mapTypeControl = new window.kakao.maps.MapTypeControl();
        map.addControl(mapTypeControl, window.kakao.maps.ControlPosition.TOPRIGHT);

        const zoomControl = new window.kakao.maps.ZoomControl();
        map.addControl(zoomControl, window.kakao.maps.ControlPosition.RIGHT);
      });
    };

    document.head.appendChild(script);

    return () => {
      if (document.head.contains(script)) {
        document.head.removeChild(script);
      }
    };
  }, [latitude, longitude, level, groupName, address]);

  return <div ref={mapRef} className='w-full h-[400px] rounded-lg mb-6' />;
}
