'use client';

import { useState, use } from 'react';
import { useRouter } from 'next/navigation';
import axios from 'axios';
import { LoginMemberContext } from '@/stores/auth/LoginMember';

interface LoginResponse {
    isSuccess: boolean;
    code: string;
    message: string;
    data: {
      id: number;
      username: string;
      nickname: string;
      role: string;
      accessToken: string;
      refreshToken: string;
    };
  }

export default function ClientPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const router = useRouter();
  const { setLoginMember } = use(LoginMemberContext);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {

        const response = await axios.post('http://localhost:8080/api/v1/members/login', {
            username,
            password
        });

        if (response.data.isSuccess) {
            const { data } = response.data;
            
            // LoginMemberContext에 사용자 정보 저장
            setLoginMember({
                id: data.id,
                username: data.username,
                password: data.password || '',
                nickname: data.nickname,
                provider: 'LOCAL',
                createdAt: data.createdAt,
                modifiedAt: data.modifiedAt,
                authorities: [data.role]
            });
    
            // 토큰을 로컬 스토리지에 저장
            localStorage.setItem('accessToken', data.accessToken);
            // localStorage.setItem('refreshToken', data.refreshToken);
            
            // axios 기본 헤더에 토큰 설정
            axios.defaults.headers.common['Authorization'] = `Bearer ${data.accessToken}`;
    
            router.push('/admin');
        } else {
            setError(response.data.message);
        }

        } catch (err) {
            setError('아이디 또는 비밀번호가 올바르지 않습니다.');
        }
    };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
      <div className="max-w-md w-full space-y-8 p-10 bg-white dark:bg-gray-800 rounded-2xl shadow-2xl">
        <div className="text-center">
          <div className="flex justify-center mb-4">
            <svg className="w-16 h-16 text-indigo-600 dark:text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
          </div>
          <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
            관리자 계정으로 로그인해주세요
          </p>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="space-y-4">
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">아이디</label>
              <input
                id="username"
                name="username"
                type="text"
                required
                className="appearance-none relative block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white rounded-lg bg-white dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors duration-200"
                placeholder="아이디를 입력하세요"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
            </div>
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">비밀번호</label>
              <input
                id="password"
                name="password"
                type="password"
                required
                className="appearance-none relative block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white rounded-lg bg-white dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-colors duration-200"
                placeholder="비밀번호를 입력하세요"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          {error && (
            <div className="text-red-500 dark:text-red-400 text-sm text-center bg-red-50 dark:bg-red-900/30 py-2 rounded-lg">
              {error}
            </div>
          )}

          <div>
            <button
              type="submit"
              className="group relative w-full flex justify-center py-2.5 px-4 border border-transparent text-sm font-medium rounded-lg text-white bg-indigo-600 hover:bg-indigo-700 dark:bg-indigo-500 dark:hover:bg-indigo-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 dark:focus:ring-offset-gray-800 transition-colors duration-200"
            >
              로그인
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}