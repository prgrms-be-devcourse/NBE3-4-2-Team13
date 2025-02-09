"use client";

import { Button } from "@/components/ui/button";
import { LoginMemberContext} from "@/stores/auth/LoginMember"; // LoginMemberContext(by React Context API) 를 통해서 공유되는 전역객체를 가져오기 위해
import { MessageCircle } from "lucide-react";
import { use, useEffect, useState } from "react"; // React Context API을 통해서 공유되는 전역객체를 가져오기 위해 사용하는 함수 `use` 로드

export default function ClientPage() {
    // frontend/src/app/ClientLayout.tsx 의 `<LoginMemberContext value={loginMemberContextValue}>` 를 통해서 공유된 value 에서 필요한 특정 값들만 가져온다.
    const { isLogin, loginMember } = use(LoginMemberContext);
    const [userNickname, setUserNickname] = useState("");

    // 프론트엔드 콜백 주소로 수정
    const KAKAO_AUTH_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${process.env.NEXT_PUBLIC_KAKAO_CLIENT_ID}&redirect_uri=${encodeURIComponent('http://localhost:3000/oauth/callback/kakao')}&response_type=code`;

    useEffect(() => {
        const accessToken = localStorage.getItem('accessToken');
        if (accessToken) {
            try {
                const payload = JSON.parse(atob(accessToken.split('.')[1]));
                // console.log('사용자 정보:', payload);
                setUserNickname(loginMember.nickname);
            } catch (error) {
                console.error('토큰 디코드 실패:', error);
                setUserNickname("사용자");
            }
        }
    }, []);

    return (
        <div className="flex-1 flex justify-center items-center">
            {(!isLogin) && (
                <Button variant="outline" asChild>
                <a
                    href={KAKAO_AUTH_URL}
                >
                    <MessageCircle />
                    <span className="font-bold">카카오 로그인</span>
                </a>
                </Button>
            )}
            {isLogin && <div>{userNickname}님 환영합니다.</div>}
        </div>
    );
}
