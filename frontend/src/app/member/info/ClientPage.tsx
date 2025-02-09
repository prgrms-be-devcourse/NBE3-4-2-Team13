"use client";
import { LoginMemberContext } from "@/stores/auth/LoginMember";
import { use, useEffect, useState } from "react";

interface Group {
    id: number;
    name: string;
    description: string;
    memberCount: number;
}

export default function ClientPage() {
    // 날짜 포맷팅 함수
    const formatDate = (dateString: string) => {
        if (!dateString) return "정보 없음";
        return dateString.split('T')[0]; 
    };

    // 권한 표시 포맷팅
    // const formatAuthorities = (authorities: Array<{authority: string}>) => {
    //     return authorities.map(auth => {
    //         switch(auth.authority) {
    //             case 'ROLE_USER':
    //                 return '일반 회원';
    //             case 'ROLE_ADMIN':
    //                 return '관리자';
    //             default:
    //                 return auth.authority;
    //         }
    //     }).join(', ');
    // };

    const { loginMember } = use(LoginMemberContext);
    const [groups, setGroups] = useState<Group[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchMyGroups = async () => {
            try {
                const token = localStorage.getItem('accessToken');
                const response = await fetch('http://localhost:8080/api/v1/members/mygroups', {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (!response.ok) {
                    throw new Error('그룹 정보를 불러오는데 실패했습니다.');
                }

                const data = await response.json();
                if (data.isSuccess) {
                    setGroups(data.data);
                } else {
                    setError(data.message);
                }
            } catch (err) {
                setError("그룹 정보를 불러오는 중 오류가 발생했습니다.");
            } finally {
                setIsLoading(false);
            }
        };

        fetchMyGroups();
    }, []);
    return (
        <div className="container mx-auto p-4 max-w-2xl">
            <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
                <div className="mb-6">
                    <h2 className="text-2xl font-bold">회원 정보</h2>
                </div>
                <div className="space-y-4">
                    <div className="grid grid-cols-3 gap-4">
                        <div className="font-semibold">닉네임</div>
                        <div className="col-span-2">{loginMember.nickname}</div>

                        <div className="font-semibold">회원 ID</div>
                        <div className="col-span-2">{loginMember.username}</div>

                        <div className="font-semibold">비밀번호</div>
                        <div className="col-span-2">{loginMember.password}</div>
                        
                        <div className="font-semibold">가입일</div>
                        <div className="col-span-2">{formatDate(loginMember.createdAt)}</div>

                        {/* <div className="font-semibold">회원 권한</div>
                        <div className="col-span-2">{formatAuthorities(loginMember.role.map(auth => ({authority: auth})))}</div> */}

                        <div className="font-semibold">가입 방식</div>
                        <div className="col-span-2">{loginMember.provider}</div>
                    </div>
                </div>
            </div>

            <div className="bg-white rounded-lg shadow-lg p-6">
                <div className="mb-6">
                    <h2 className="text-2xl font-bold">내 그룹 목록</h2>
                </div>
                
                {isLoading ? (
                    <div className="text-center py-4">로딩 중...</div>
                ) : error ? (
                    <div className="text-red-500 text-center py-4">{error}</div>
                ) : groups.length === 0 ? (
                    <div className="text-center py-4 text-gray-500">
                        아직 가입한 그룹이 없습니다.
                    </div>
                ) : (
                    <div className="space-y-4">
                        {groups.map((group) => (
                            <div 
                                key={group.id} 
                                className="border rounded-lg p-4 hover:bg-gray-50 transition-colors"
                            >
                                <div className="flex justify-between items-center">
                                    <div>
                                        <h3 className="font-semibold text-lg">{group.name}</h3>
                                        <p className="text-gray-600">{group.description}</p>
                                    </div>
                                    <div className="text-sm text-gray-500">
                                        멤버 {group.memberCount}명
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}