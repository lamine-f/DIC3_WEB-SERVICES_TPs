<?php
declare(strict_types=1);

namespace App\Client;

/**
 * Port partage entre les phases SOAP et REST.
 * Les pages du frontend ne dependent que de cette interface.
 */
interface ClientInterface
{
    public function register(string $username, string $password): string;

    public function login(string $username, string $password): string;

    public function logout(string $token): bool;

    /** @return array<int,array{userId:int,username:string}> */
    public function validateToken(string $token): array;

    /** @return array<int,array{id:int,name:string,createdBy:int}> */
    public function listRooms(string $token): array;

    /** @return array{id:int,name:string,createdBy:int} */
    public function createRoom(string $token, string $name): array;

    public function sendMessage(string $token, int $roomId, string $content): int;

    /** @return array<int,array{id:int,roomId:int,userId:int,username:string,content:string,createdAt:string}> */
    public function getMessages(string $token, int $roomId, int $sinceId): array;
}
