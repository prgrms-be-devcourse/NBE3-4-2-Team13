'use client';

import { useEffect, useRef } from 'react';

interface Location {
  id: number;
  name: string;
  latitude: string;
  longitude: string;
  address: string;
}

interface KakaoMapMultiMarkerProps {
  locations: Location[];
  onMarkerClick?: (groupId: number) => void;
}

interface GroupedLocation {
  latitude: string;
  longitude: string;
  address: string;
  groups: Location[];
}

declare global {
  interface Window {
    kakao: any;
  }
}

export default function KakaoMapMultiMarker({ locations, onMarkerClick }: KakaoMapMultiMarkerProps) {
  const mapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const script = document.createElement('script');
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_MAP_KEY}&libraries=services&autoload=false`;

    script.onload = () => {
      window.kakao.maps.load(() => {
        if (!mapRef.current || locations.length === 0) return;

        // 초기 지도 생성
        const map = new window.kakao.maps.Map(mapRef.current, {
          center: new window.kakao.maps.LatLng(37.5665, 126.978), // 서울 시청 좌표로 초기화
          level: 8,
        });

        // 위치별로 그룹 묶기
        const groupedLocations: GroupedLocation[] = locations.reduce((acc: GroupedLocation[], loc) => {
          const existingGroup = acc.find(
            (group) => group.latitude === loc.latitude && group.longitude === loc.longitude
          );

          if (existingGroup) {
            existingGroup.groups.push(loc);
          } else {
            acc.push({
              latitude: loc.latitude,
              longitude: loc.longitude,
              address: loc.address,
              groups: [loc],
            });
          }
          return acc;
        }, []);

        // 모든 위치의 좌표를 LatLng 객체로 변환
        const bounds = new window.kakao.maps.LatLngBounds();

        // 각 위치에 마커와 오버레이 생성
        groupedLocations.forEach((location) => {
          const position = new window.kakao.maps.LatLng(parseFloat(location.latitude), parseFloat(location.longitude));

          bounds.extend(position);

          // 커스텀 오버레이용 div 엘리먼트 생성
          const overlayContent = document.createElement('div');
          overlayContent.className = 'custom-overlay bg-white dark:bg-gray-800 p-3 rounded-lg shadow-lg cursor-pointer';

          const updateOverlayContent = (isZoomedIn: boolean) => {
            if (isZoomedIn) {
              overlayContent.innerHTML = `
                <div class="space-y-2">
                  ${location.groups
                    .map(
                      (g) => `
                      <div class="group-item" data-group-id="${g.id}">
                        <h3 class="font-bold text-gray-900 dark:text-white">${g.name}</h3>
                      </div>
                    `
                    )
                    .join('')}
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-300 mt-2">${location.address}</p>
              `;

              // 각 모임 항목에 클릭 이벤트 추가
              overlayContent.querySelectorAll('.group-item').forEach((element) => {
                element.addEventListener('click', (e) => {
                  e.stopPropagation();
                  const groupId = parseInt(element.getAttribute('data-group-id') || '0');
                  if (groupId && onMarkerClick) {
                    onMarkerClick(groupId);
                  }
                });
              });
            } else {
              overlayContent.innerHTML = `
                <h3 class="font-bold text-gray-900 dark:text-white">
                  ${location.groups.length === 1 ? location.groups[0].name : `모임 ${location.groups.length}개`}
                </h3>
                <p class="text-sm text-gray-600 dark:text-gray-300">${location.address}</p>
                ${
                  location.groups.length > 1
                    ? `<div class="text-xs text-blue-600 dark:text-blue-400 mt-1">
                        ${location.groups.map((g) => g.name).join(', ')}
                      </div>`
                    : ''
                }
              `;
            }
          };

          // 초기 컨텐츠 설정
          updateOverlayContent(false);

          // 오버레이에 클릭 이벤트 추가
          overlayContent.addEventListener('click', () => {
            if (location.groups.length === 1 && onMarkerClick) {
              onMarkerClick(location.groups[0].id);
            } else {
              map.setCenter(position);
              map.setLevel(3);
            }
          });

          // 커스텀 오버레이 생성
          const overlay = new window.kakao.maps.CustomOverlay({
            map: map,
            position: position,
            content: overlayContent,
            yAnchor: 1,
          });

          // 줌 레벨 변경 이벤트 리스너
          window.kakao.maps.event.addListener(map, 'zoom_changed', () => {
            const level = map.getLevel();
            updateOverlayContent(level < 5);
          });
        });

        // 모든 마커가 보이도록 지도 범위 재설정
        if (groupedLocations.length > 0) {
          map.setBounds(bounds, 100); // 여백 100px 추가
        }

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
  }, [locations, onMarkerClick]);

  return <div ref={mapRef} className='w-full h-[400px] rounded-lg mb-6' />;
}
