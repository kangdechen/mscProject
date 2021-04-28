class UserModel:
  def __init__(self, UserId,name,email,passwd,gender,weight,age,plan,picurl,height ):

    self.UserId=UserId
    self.name = name
    self.email=email
    self.passwd=passwd
    self.gender=gender
    self.weight=weight
    self.age=age
    self.plan=plan
    self.picurl=picurl
    self.height=height



class FoodModel:
  def __init__(self,Id,title,rating,calories,protien,fat,sodium):
      self.Id=Id
      self.title=title
      self.rating=rating
      self.calories=calories
      self.protien=protien
      self.fat=fat
      self.sodium=sodium


class FoodInfoModel:
  def __init__(self,Id,FoodId,Ingredient):
      self.Id=Id
      self.FoodId=FoodId
      self.Ingredient=Ingredient
