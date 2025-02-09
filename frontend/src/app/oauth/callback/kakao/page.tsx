'use client';

import { useEffect, useRef, use } from 'react';
import { useRouter } from 'next/navigation';
import { LoginMemberContext } from '@/stores/auth/LoginMember';

export default function KakaoCallback() {
    const router = useRouter();
    const isCallbackProcessed = useRef(false);
    const { setLoginMember } = use(LoginMemberContext);

    useEffect(() => {
        const handleKakaoCallback = async () => {
            // 이미 처리된 경우 return
            if (isCallbackProcessed.current) return;
            
            try {
                isCallbackProcessed.current = true;  // 처리 시작 표시
                const code = new URL(window.location.href).searchParams.get('code');
                
                if (!code) {
                    throw new Error('인증 코드가 없습니다.');
                }

                const response = await fetch(`http://localhost:8080/api/v1/members/kakao/callback?code=${code}`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                    },
                    credentials: 'include',
                    mode: 'cors'
                });

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();

                if (data.accessToken) {  // accessToken이 있는 경우에만 저장
                    // 응답 데이터를 localStorage에 저장
                    Object.entries(data).forEach(([key, value]) => {
                        localStorage.setItem(key, String(value));
                    });

                    // accessToken으로 사용자 정보 조회
                    const userResponse = await fetch('http://localhost:8080/api/v1/members/info', {
                        headers: {
                            'Authorization': `Bearer ${data.accessToken}`
                        }
                    });

                    if (userResponse.ok) {
                        const userData = await userResponse.json();
                        // LoginMemberContext 업데이트
                        setLoginMember({
                            id: userData.data.id,
                            username: userData.data.username,
                            password: userData.data.password || "-",
                            nickname: userData.data.nickname,
                            createdAt: userData.data.createdAt,
                            provider: userData.data.provider,
                            authorities: userData.data.authorities || [],
                            modifiedAt: userData.data.modifiedAt
                        });
                    }
                    router.push('/');  // 성공시에만 리다이렉트
                } else {
                    throw new Error('액세스 토큰이 없습니다.');
                }

                
            } catch (error) {
                console.error('카카오 로그인 처리 중 오류:', error);
                isCallbackProcessed.current = false;  // 에러 발생시 재시도 가능하도록
                router.push('/');  // 에러시 로그인 페이지로 이동
            }
        };

        handleKakaoCallback();
    }, [router]);

    return (
        <div className="flex justify-center items-center min-h-screen">
            <div className="text-center">
                <h2 className="text-xl font-bold mb-4">로그인 처리 중...</h2>
                <p>잠시만 기다려주세요.</p>
            </div>
        </div>
    );
}