"use client";

import { useEffect, useState } from "react";
import axios from "axios";
import { useRouter } from "next/navigation";

interface Member {
    id: number;
    username: string;
    nickname: string;
    provider: string | null;
    role: string;
    disabled: boolean;
    createdAt: number[];
    modifiedAt: number[];
}

export default function ClientPage() {
    const [members, setMembers] = useState<Member[]>([]);
    const router = useRouter();

    useEffect(() => {
        const fetchMembers = async () => {
            try {
                const token = localStorage.getItem('accessToken');
                if (!token) {
                    throw new Error('인증 토큰이 없습니다.');
                }

                const response = await axios.get('http://localhost:8080/api/v1/members/findAll', {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    withCredentials: true
                });
                
                if (response.data.isSuccess) {
                    setMembers(response.data.data);
                }
            } catch (error) {
                console.error('멤버 정보를 가져오는데 실패했습니다:', error);
            }
        };

        fetchMembers();
    }, []);

    return (
        <div className="min-h-screen bg-gray-50">
            {/* 상단 네비게이션 바 */}
            <div className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between h-16 items-center">
                        <h1 className="text-xl font-bold text-gray-900">관리자 대시보드</h1>
                        <div className="flex space-x-4">
                            <button
                                onClick={() => router.push('/admin/members')}
                                className="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-md hover:bg-gray-100"
                            >
                                회원 관리
                            </button>
                            <button
                                onClick={() => router.push('/admin/categories')}
                                className="px-4 py-2 text-sm font-medium text-gray-900 bg-white border border-gray-200 rounded-md hover:bg-gray-100"
                            >
                                카테고리 관리
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* 메인 컨텐츠 */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="bg-white shadow rounded-lg">
                    <div className="px-4 py-5 sm:px-6 border-b border-gray-200">
                        <h2 className="text-lg font-medium text-gray-900">회원 목록</h2>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">사용자명</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">닉네임</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">가입 경로</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">권한</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">가입일</th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {members.map((member) => (
                                    <tr key={member.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{member.id}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{member.username}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{member.nickname}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{member.provider || '일반'}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{member.role}</td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm">
                                            <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${member.disabled ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'}`}>
                                                {member.disabled ? '비활성화' : '활성화'}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                            {new Date(member.createdAt[0], member.createdAt[1] - 1, member.createdAt[2]).toLocaleDateString()}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}
