package polyClinicSystem.example.prescription_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import polyClinicSystem.example.prescription_service.dto.request.CreateMedicalRecord;
import polyClinicSystem.example.prescription_service.dto.request.CreatePrescription;
import polyClinicSystem.example.prescription_service.dto.request.UpdatePrescription;
import polyClinicSystem.example.prescription_service.dto.response.MedicalRecordResponse;
import polyClinicSystem.example.prescription_service.dto.response.PrescriptionResponse;
import polyClinicSystem.example.prescription_service.dto.response.PrescriptionSlimDTO;
import polyClinicSystem.example.prescription_service.model.MedicalRecord;
import polyClinicSystem.example.prescription_service.model.Prescription;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MapperSystem {

    @Mapping(source = "medicalRecord.id", target = "recordId")
    PrescriptionResponse toPrescriptionResponse(Prescription prescription);

    // Convert CreatePrescription -> Prescription (id + medicalRecord ignored)
    //cause there is relationships and mapping between medicalRecord and prescription
    //and this will get conflict to map struct
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    Prescription toPrescription(CreatePrescription request);

    MedicalRecord toMedicalRecord(CreateMedicalRecord request);

    @Mapping(source = "prescriptions", target = "prescriptions")
    MedicalRecordResponse toMedicalRecordResponse(MedicalRecord medicalRecord);

    PrescriptionSlimDTO toPrescriptionSlim(Prescription prescription);
}
