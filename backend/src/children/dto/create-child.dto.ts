import { IsString, MinLength, MaxLength, IsOptional, IsInt, Min, Max } from 'class-validator';

export class CreateChildDto {
  @IsString()
  @MinLength(1)
  @MaxLength(50)
  displayName: string;

  @IsOptional()
  @IsInt()
  @Min(2000)
  @Max(2024)
  birthYear?: number;
}
