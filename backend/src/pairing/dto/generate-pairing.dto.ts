import { IsUUID } from 'class-validator';

export class GeneratePairingDto {
  @IsUUID()
  childId: string;
}
