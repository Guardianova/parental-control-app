import { IsString, IsNotEmpty, IsIn, IsOptional, MaxLength } from 'class-validator';

export class PairDeviceDto {
  @IsString()
  @IsNotEmpty()
  qrPayload: string;

  @IsIn(['android', 'ios'])
  platform: string;

  @IsOptional()
  @IsString()
  @MaxLength(100)
  deviceModel?: string;

  @IsString()
  @IsNotEmpty()
  publicKey: string;
}
