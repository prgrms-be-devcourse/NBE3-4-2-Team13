"use client";
import { ThemeProvider as NextThemesProvider } from "next-themes";
import * as React from "react";
import {
  Moon,
  Sun,
  Home,
  LogIn,
  LogOut,
  Settings,
  User,
  Users,
  MessageSquare,
} from "lucide-react";
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
import { notificationService } from "../services/notificationService";
import { NotificationBell } from "@/components/notification/NotificationBell";

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

export function ClientLayout({
  children,
}: React.ComponentProps<typeof NextThemesProvider>) {
  const router = useRouter();
  const INITIAL_SESSION_TIME = 30 * 60; // 30분
  const [sessionTime, setSessionTime] = React.useState<number>(0);
  const [tokenExpirationTime, setTokenExpirationTime] =
    React.useState<number>(0); // 토큰 만료 시간을 저장할 state

  // formatTime 함수를 컴포넌트 내부로 이동
  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, "0")}`;
  };

  // 시간 연장 함수 수정
  const extendTime = () => {
    setSessionTime(INITIAL_SESSION_TIME);
  };

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

  const logout = async () => {
    try {
      const token = localStorage.getItem("accessToken");
      if (!token) {
        setNoLoginMember();
        return;
      }
      // 백엔드에 로그아웃 요청
      const response = await fetch(
        "http://localhost:8080/api/v1/members/logout",
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
          credentials: "include", // refreshToken이 쿠키에 있으므로 필요
        }
      );
      if (!response.ok) {
        throw new Error("로그아웃 실패");
      }

      // 프론트엔드 정리
      localStorage.removeItem("accessToken");
      document.cookie =
        "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
      removeLoginMember();
      router.replace("/");
    } catch (error) {
      console.error("로그아웃 중 에러 발생:", error);
      // 백엔드 에러가 발생하더라도 프론트엔드는 로그아웃 처리
      localStorage.removeItem("accessToken");
      document.cookie =
        "refreshToken=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
      removeLoginMember();
      router.replace("/");
    }
  };

  useEffect(() => {
    // 초기 로딩 시 토큰 존재 여부에 따라 로그인 상태 설정
    const token = localStorage.getItem("accessToken");
    if (!token) {
      setNoLoginMember();
      return;
    }

    try {
      // JWT 토큰에서 페이로드 추출
      const payload = JSON.parse(atob(token.split('.')[1]));
      
      // 토큰이 만료되었는지 확인
      const currentTime = Math.floor(Date.now() / 1000);
      if (payload.exp && payload.exp < currentTime) {
        // 토큰이 만료된 경우
        localStorage.removeItem("accessToken");
        setNoLoginMember();
        return;
      }

      // 토큰이 유효하면 사용자 정보를 가져옴
      fetch("http://localhost:8080/api/v1/members/info", {
        headers: {
          Authorization: `Bearer ${token}`
        }
      })
      .then(response => response.json())
      .then(data => {
        if (data.isSuccess) {
          setLoginMember({
            id: data.data.id,
            username: data.data.username,
            password: data.data.password || "-",
            nickname: data.data.nickname,
            createdAt: data.data.createdAt,
            provider: data.data.provider,
            authorities: data.data.authorities || [],
            modifiedAt: data.data.modifiedAt
          });
        } else {
          setNoLoginMember();
        }
      })
      .catch(error => {
        console.error("사용자 정보 조회 중 에러:", error);
        setNoLoginMember();
      });

    } catch (error) {
      console.error("토큰 처리 중 에러:", error);
      setNoLoginMember();
    }
  }, []); // 컴포넌트 마운트 시 한 번만 실행

  useEffect(() => {
    if (isLogin) {
      setSessionTime(INITIAL_SESSION_TIME);
      notificationService.connect();

      // 컴포넌트 언마운트나 로그아웃 시 연결 해제
      return () => {
        notificationService.disconnect();
      };
    }
  }, [isLogin]); // 로그인 상태가 변경될 때만 실행

  useEffect(() => {
    // 로그인 상태일 때만 타이머 실행
    let timer: NodeJS.Timeout;
    if (isLogin && sessionTime > 0) {
      timer = setInterval(() => {
        setSessionTime((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            logout(); // 시간 종료시 자동 로그아웃
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    }

    // 컴포넌트가 언마운트되거나 의존성이 변경될 때 타이머 정리
    return () => {
      if (timer) clearInterval(timer);
    };
  }, [isLogin, sessionTime]); // sessionTime 의존성 추가

  useEffect(() => {
    // 로그인된 상태일 때만 SSE 연결
    if (isLogin) {
      // 브라우저 알림 권한 요청
      if (Notification.permission === 'default') {
        Notification.requestPermission();
      }

      // SSE 연결
      notificationService.connect();

      // 컴포넌트 언마운트나 로그아웃 시 연결 해제
      return () => {
        notificationService.disconnect();
      };
    }
  }, [isLogin]); // isLogin이 변경될 때마다 실행

  if (isLoginMemberPending) {
    return (
      <div className="flex-1 flex justify-center items-center text-muted-foreground">
        인증 정보 로딩중...
      </div>
    );
  }

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
        <header className="border-b">
          <div className="container flex items-center justify-between h-14">
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
            <div className="flex items-center gap-4">
              {isLogin && <NotificationBell />}
              {isLogin && (
                <div className="flex items-center gap-2">
                  <span className="text-sm text-muted-foreground">
                    세션 만료까지: {formatTime(sessionTime)}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={extendTime}
                    className="text-xs"
                  >
                    시간연장
                  </Button>
                </div>
              )}
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
