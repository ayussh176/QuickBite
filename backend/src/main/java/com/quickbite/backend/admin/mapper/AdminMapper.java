package com.quickbite.backend.admin.mapper;

import com.quickbite.backend.admin.dto.ComplaintResponse;
import com.quickbite.backend.admin.dto.UserManagementResponse;
import com.quickbite.backend.admin.entity.Complaint;
import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.config.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface AdminMapper {

    @Mapping(target = "status", source = "accountStatus")
    UserManagementResponse toUserResponse(User user);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(complaint.getCustomer() != null ? complaint.getCustomer().getFirstName() + \" \" + complaint.getCustomer().getLastName() : null)")
    @Mapping(target = "customerPhone", source = "customer.user.phone")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderNumber", source = "order.orderNumber")
    ComplaintResponse toComplaintResponse(Complaint complaint);
}
