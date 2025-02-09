"use client";
import { ThemeProvider as NextThemesProvider } from "next-themes";
import * as React from "react";
import { Moon, Sun, Home, LogIn, LogOut, Settings, User, Users, MessageSquare } from "lucide-react";
import { useTheme } from "next-themes";
import { LoginMemberContext, useLoginMember } from "@/stores/auth/LoginMember";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

function ModeToggle() {
  const { setTheme } = useTheme();
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" size="icon">
          <Sun className="h-[1.2rem] w-[1.2rem] rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
          <Moon className="absolute h-[1.2rem] w-[1.2rem] rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
          <span className="sr-only">테마</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={() => setTheme("light")}>
          Light
        </DropdownMenuItem>
        <DropdownMenuItem onClick={() => setTheme("dark")}>
          Dark
        </DropdownMenuItem>
        <DropdownMenuItem onClick={() => setTheme("system")}>
          System
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

export function ClientLayout({children,}: React.ComponentProps<typeof NextThemesProvider>) {
  // 로그아웃 후 홈으로 이동하기 위해서 로드
  const router = useRouter();

  // 훅을 통해서 로그인 한 회원의 정보(state)와 관련된 함수들을 얻는다.
  const {
    setLoginMember,
    isLogin,
    loginMember,
    removeLoginMember,
    isLoginMemberPending,
    isAdmin,
    setNoLoginMember,
  } = useLoginMember();

  // Context를 통해서 전역적으로 공유할 값을 만든다.
  const loginMemberContextValue = {
    loginMember,
    setLoginMember,
    removeLoginMember,
    isLogin,
    isLoginMemberPending,
    isAdmin,
    setNoLoginMember,
  };

  useEffect(() => {
    // 추후 이 부분은 fetch(GET http://localhost:8080/api/v1/members/me) 로 대체될 예정이다.
    // 현재는 일단은 임시로 관리자 회원으로 로그인 했다고 가정하기 위해서 아래와 같이 선언
    // 실제로 나중에 fetch로 변경될 때도 fetch가 완료되는데 걸리는 시간이 걸린다.
    // 그 상황을 실감나게 흉내내기 위해서 setTimeout을 사용하였다.
    const fetchLoginMember = async () => {
      try {
        const token = localStorage.getItem('accessToken');

        const response = await fetch("http://localhost:8080/api/v1/members/info", {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          credentials: 'include'
        });
        if (response.ok) {
          const responseData = await response.json();
          
          if (responseData.isSuccess) {
            const memberData = responseData.data;
            setLoginMember({
              id: memberData.id,
              username: memberData.username,
              password: memberData.password,
              nickname: memberData.nickname,
              createdAt: memberData.createdAt,
              modifiedAt: memberData.modifiedAt,
              provider: memberData.provider,
              authorities: memberData.authorities.map((auth: any) => auth.authority), // authorities 배열에서 authority 값만 추출
            });
          } else {
            setNoLoginMember();
          }
        } else {
          setNoLoginMember();
        }
      } catch (error) {
        console.error("로그인 정보 조회 실패:", error);
        setNoLoginMember();
      }
    };
    fetchLoginMember();
  }, []);


  // isLoginMemberPending 의 시작상태는 true 이다.
  // 해당값이 true 라는 것은 아직 로그인 상태인지 아닌지 판별되기 전이라는 의미이다.
  // setLoginMember, setNoLoginMember, removeLoginMember 함수가 호출되면 해당값이 false 로 변경된다.
  if (isLoginMemberPending) {
    return (
      <div className="flex-1 flex justify-center items-center text-muted-foreground">
        인증 정보 로딩중...
      </div>
    );
  }

  const logout = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setNoLoginMember();
        return;
      }
      // 백엔드에 로그아웃 요청
      const response = await fetch('http://localhost:8080/api/v1/members/logout', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        credentials: 'include'  // refreshToken이 쿠키에 있으므로 필요
      });
      if (!response.ok) {
        throw new Error('로그아웃 실패');
      }

      // 프론트엔드 정리
      localStorage.removeItem('accessToken');
      localStorage.removeItem('nickname');
      document.cookie = 'refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
      removeLoginMember();
      router.replace("/");
      
    } catch (error) {
      console.error('로그아웃 중 에러 발생:', error);
      // 백엔드 에러가 발생하더라도 프론트엔드는 로그아웃 처리
      localStorage.removeItem('accessToken');
      localStorage.removeItem('nickname');
      document.cookie = 'refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
      removeLoginMember();
      router.replace("/");
    }
  };

  return (
    <NextThemesProvider
      attribute="class"
      defaultTheme="system"
      enableSystem
      disableTransitionOnChange
    >
      <LoginMemberContext value={loginMemberContextValue}>
        {/* 이 안의 모든 곳에서 `loginMemberContextValue` 변수를 `use` 함수를 통해서 접근할 수 있다. */}
        {/* 하지만 여기서는 어짜피 useLoginMember 함수의 실행결과가 바로 있기 때문에 딱히 `use` 를 사용할 필요가 없다. */}
        <header className="flex p-2 items-center">
          <div className="flex">
            <Button variant="link" asChild>
              <Link href="/">
                <Home /> LinkUs
              </Link>
            </Button>
            {isLogin && (
              <>
                <Button variant="link" asChild>
                  <Link href="/groups">
                    <Users /> 모임
                  </Link>
                </Button>
                <Button variant="link" asChild>
                  <Link href="/chat">
                    <MessageSquare /> 채팅
                  </Link>
                </Button>
              </>
            )}
          </div>
          <div className="flex-grow"></div>
          <div className="flex items-center gap-2">
            {isLogin && (
              <Button variant="link" asChild>
                <Link href="/member/info">
                  <User /> 내 정보
                </Link>
              </Button>
            )}
            {isLogin && (
              <Button variant="link" onClick={logout}>
                <LogOut /> 로그아웃
              </Button>
            )}
            <ModeToggle />
          </div>
        </header>
        <main className="flex-1 flex flex-col">{children}</main>
        <footer className="p-2 flex justify-center">
          <Button variant="link" asChild>
            <Link href="/admin">
              <Settings /> 관리자
            </Link>
          </Button>
          <Button variant="link" asChild>
            <Link href="/admin/login">
              <LogIn /> 관리자 로그인
            </Link>
          </Button>
        </footer>
      </LoginMemberContext>
    </NextThemesProvider>
  );
}